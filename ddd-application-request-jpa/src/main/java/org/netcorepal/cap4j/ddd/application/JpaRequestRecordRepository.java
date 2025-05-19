package org.netcorepal.cap4j.ddd.application;

import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.application.persistence.ArchivedRequest;
import org.netcorepal.cap4j.ddd.application.persistence.ArchivedRequestJpaRepository;
import org.netcorepal.cap4j.ddd.application.persistence.Request;
import org.netcorepal.cap4j.ddd.application.persistence.RequestJpaRepository;
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
 * Jpa请求记录仓库
 *
 * @author binking338
 * @date 2025/5/16
 */
@RequiredArgsConstructor
public class JpaRequestRecordRepository implements RequestRecordRepository {
    private final RequestJpaRepository requestJpaRepository;
    private final ArchivedRequestJpaRepository archivedRequestJpaRepository;
    @Override
    public RequestRecord create() {
        return new RequestRecordImpl();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(RequestRecord requestRecord) {
        RequestRecordImpl requestRecordImpl = (RequestRecordImpl) requestRecord;
        Request request = requestRecordImpl.getRequest();
        request = requestJpaRepository.saveAndFlush(request);
        requestRecordImpl.resume(request);
    }

    @Override
    public RequestRecord getById(String id) {
        Request request = requestJpaRepository.findOne((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Request.F_REQUEST_UUID), id))
                .orElseThrow(() -> new DomainException("RequestRecord not found"));
        RequestRecordImpl requestRecordImpl = new RequestRecordImpl();
        requestRecordImpl.resume(request);
        return requestRecordImpl;
    }

    @Override
    public List<RequestRecord> getByNextTryTime(String svcName, LocalDateTime maxNextTryTime, int limit) {
        Page<Request> requests = requestJpaRepository.findAll((root, cq, cb) -> {
            cq.where(cb.or(
                    cb.and(
                            // 【初始状态】
                            cb.equal(root.get(Request.F_REQUEST_STATE), Request.RequestState.INIT),
                            cb.lessThan(root.get(Request.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Request.F_SVC_NAME), svcName)
                    ), cb.and(
                            // 【执行中状态】
                            cb.equal(root.get(Request.F_REQUEST_STATE), Request.RequestState.EXECUTING),
                            cb.lessThan(root.get(Request.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Request.F_SVC_NAME), svcName)
                    ), cb.and(
                            // 【异常状态】
                            cb.equal(root.get(Request.F_REQUEST_STATE), Request.RequestState.EXCEPTION),
                            cb.lessThan(root.get(Request.F_NEXT_TRY_TIME), maxNextTryTime),
                            cb.equal(root.get(Request.F_SVC_NAME), svcName)
                    )));
            return null;
        }, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, Request.F_NEXT_TRY_TIME)));
        return requests.stream().map(request -> {
            RequestRecordImpl requestRecordImpl = new RequestRecordImpl();
            requestRecordImpl.resume(request);
            return requestRecordImpl;
        }).collect(Collectors.toList());
    }

    @Override
    public int archiveByExpireAt(String svcName, LocalDateTime maxExpireAt, int limit) {
        Page<Request> requests = requestJpaRepository.findAll((root, cq, cb) -> {
            cq.where(
                    cb.and(
                            // 【状态】
                            cb.or(
                                    cb.equal(root.get(Request.F_REQUEST_STATE), Request.RequestState.CANCEL),
                                    cb.equal(root.get(Request.F_REQUEST_STATE), Request.RequestState.EXPIRED),
                                    cb.equal(root.get(Request.F_REQUEST_STATE), Request.RequestState.EXHAUSTED),
                                    cb.equal(root.get(Request.F_REQUEST_STATE), Request.RequestState.EXECUTED)
                            ),
                            cb.lessThan(root.get(Request.F_EXPIRE_AT), maxExpireAt),
                            cb.equal(root.get(Request.F_SVC_NAME), svcName)
                    ));
            return null;
        }, PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, Request.F_NEXT_TRY_TIME)));
        if(!requests.hasContent()){
            return 0;
        }
        List<ArchivedRequest> archivedRequests = requests.stream().map(request -> {
            ArchivedRequest archivedRequest = new ArchivedRequest();
            archivedRequest.archiveFrom(request);
            return archivedRequest;
        }).collect(Collectors.toList());
        migrate(requests.getContent(), archivedRequests);
        return requests.getNumberOfElements();
    }

    @Transactional
    public void migrate(List<Request> requests, List<ArchivedRequest> archivedRequests) {
        archivedRequestJpaRepository.saveAll(archivedRequests);
        requestJpaRepository.deleteInBatch(requests);
    }
}
