package org.ddd.share;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 类扫描工具
 *
 * @author <template/>
 * @date 2023-03-25
 */
public class ScanUtils {
    private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory METADATA_READER_FACTORY = new SimpleMetadataReaderFactory();

    /**
     * 扫描指定路径下的所有类
     *
     * @param scanPath     扫描路径
     * @param concrete     扫描的类是否是具体的类，即非接口、非抽象类
     * @return 扫描到的类
     */
    @lombok.SneakyThrows
    public static Set<Class<?>> scanClass(String scanPath, boolean concrete) {
        String path = ClassUtils.convertClassNameToResourcePath(scanPath);
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + path + "/**/*.class";

        Set<Class<?>> classes = new HashSet<>();
        Resource[] resources = RESOLVER.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = METADATA_READER_FACTORY.getMetadataReader(resource);
                ClassMetadata classMetadata = metadataReader.getClassMetadata();
                if (!concrete || classMetadata.isConcrete()) {
                    classes.add(Class.forName(classMetadata.getClassName()));
                }
            }
        }
        return classes;
    }
}
