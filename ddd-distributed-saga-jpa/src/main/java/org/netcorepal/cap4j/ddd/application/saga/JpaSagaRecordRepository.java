package org.netcorepal.cap4j.ddd.application.saga;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.saga.persistence.ArchivedSaga;
import org.netcorepal.cap4j.ddd.application.saga.persistence.ArchivedSagaJpaRepository;
import org.netcorepal.cap4j.ddd.application.saga.persistence.Saga;
import org.netcorepal.cap4j.ddd.application.saga.persistence.SagaJpaRepository;
import org.netcorepal.cap4j.ddd.share.DomainException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Saga记录仓储
 *
 * @author binking338
 * @date 2024/10/12
 */
@RequiredArgsConstructor
public class JpaSagaRecordRepository implements SagaRecordRepository {
    private final SagaJpaRepository sagaJpaRepository;
    private final ArchivedSagaJpaRepository archivedSagaJpaRepository;

    @Override
    public SagaRecord create() {
        return new SagaRecordImpl();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(SagaRecord sagaRecord) {
        SagaRecordImpl sagaRecordImpl = (SagaRecordImpl) sagaRecord;
        Saga saga = sagaRecordImpl.getSaga();
        saga = sagaJpaRepository.saveAndFlush(saga);
        sagaRecordImpl.resume(saga);
    }

    @Override
    public SagaRecord getById(String id) {
        Saga saga = sagaJpaRepository.findOne((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Saga.F_SAGA_UUID), id))
                .orElseThrow(() -> new DomainException("SagaRecord not found"));
        SagaRecordImpl sagaRecordImpl = new SagaRecordImpl();
        sagaRecordImpl.resume(saga);
        return sagaRecordImpl;
    }

    @Override
    public List<SagaRecord> getByNextTryTime(String svcName, LocalDateTime maxNextTryTime, int limit) {
        Page<Saga> sagas = sagaJpaRepository.findAll((root, cq, cb) -> {
            cq.where(cb.or(
                    cb.and(
                            // 【初始状态】
                            cb.equal(root.get(Saga.F_SAGA_STATE), Saga.SagaState.INIT),
                            cb.lessThan(root.get(Saga.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Saga.F_SVC_NAME), svcName)
                    ), cb.and(
                            // 【执行中状态】
                            cb.equal(root.get(Saga.F_SAGA_STATE), Saga.SagaState.EXECUTING),
                            cb.lessThan(root.get(Saga.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Saga.F_SVC_NAME), svcName)
                    ), cb.and(
                            // 【异常状态】
                            cb.equal(root.get(Saga.F_SAGA_STATE), Saga.SagaState.EXCEPTION),
                            cb.lessThan(root.get(Saga.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Saga.F_SVC_NAME), svcName)
                    )));
            return null;
        }, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, Saga.F_NEXT_TRY_TIME)));
        return sagas.stream().map(saga -> {
            SagaRecordImpl sagaRecordImpl = new SagaRecordImpl();
            sagaRecordImpl.resume(saga);
            return sagaRecordImpl;
        }).collect(Collectors.toList());
    }

    @Override
    public int archiveByExpireAt(String svcName, LocalDateTime maxExpireAt, int limit) {
        Page<Saga> sagas = sagaJpaRepository.findAll((root, cq, cb) -> {
            cq.where(
                    cb.and(
                            // 【状态】
                            cb.or(
                                    cb.equal(root.get(Saga.F_SAGA_STATE), Saga.SagaState.CANCEL),
                                    cb.equal(root.get(Saga.F_SAGA_STATE), Saga.SagaState.EXPIRED),
                                    cb.equal(root.get(Saga.F_SAGA_STATE), Saga.SagaState.EXHAUSTED),
                                    cb.equal(root.get(Saga.F_SAGA_STATE), Saga.SagaState.EXECUTED)
                            ),
                            cb.lessThan(root.get(Saga.F_EXPIRE_AT), maxExpireAt),
                            cb.equal(root.get(Saga.F_SVC_NAME), svcName)
                    ));
            return null;
        }, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, Saga.F_NEXT_TRY_TIME)));
        if(!sagas.hasContent()){
            return 0;
        }
        List<ArchivedSaga> archivedSagas = sagas.stream().map(saga -> {
            ArchivedSaga archivedSaga = new ArchivedSaga();
            archivedSaga.archiveFrom(saga);
            return archivedSaga;
        }).collect(Collectors.toList());
        migrate(sagas.getContent(), archivedSagas);
        return sagas.getNumberOfElements();
    }

    @Transactional
    public void migrate(List<Saga> sagas, List<ArchivedSaga> archivedSagas) {
        archivedSagaJpaRepository.saveAll(archivedSagas);
        sagaJpaRepository.deleteInBatch(sagas);
    }
}
