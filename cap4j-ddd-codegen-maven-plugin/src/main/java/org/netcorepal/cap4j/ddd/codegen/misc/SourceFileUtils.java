package org.netcorepal.cap4j.ddd.codegen.misc;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;

/**
 * @author binking338
 * @date 2022-02-17
 */
public class SourceFileUtils {
    static final String PACKAGE_SPLITTER = ".";

    private static Map<String, List<File>> Cache = new HashMap<>();
    private final static String SRC_MAIN_JAVA = "src.main.java.";
    private final static String SRC_TEST_JAVA = "src.test.java.";

    /**
     * 获取所有文件清单，含子目录中的文件
     *
     * @param baseDir
     * @return
     */
    public static List<File> loadFiles(String baseDir) {
        if (Cache.containsKey(baseDir)) {
            return Cache.get(baseDir);
        }
        File file = new File(baseDir);
        LinkedList<File> list = new LinkedList<>();
        List<File> result = new ArrayList<>();
        if (file.exists()) {
            if (null == file.listFiles()) {
                result.add(file);
                return result;
            }
            list.addAll(Arrays.asList(file.listFiles()));
            while (!list.isEmpty()) {
                File file1 = list.removeFirst();
                File[] files = file1.listFiles();
                if (null == files) {
                    result.add(file1);
                    continue;
                }
                for (File f : files) {
                    if (f.isDirectory()) {
                        list.add(f);
                    } else {
                        result.add(f);
                    }
                }
            }
        } else {
            throw new RuntimeException("文件夹不存在！");
        }
        Cache.put(baseDir, result);
        return result;
    }

    /**
     * 加载文件内容(支持FilePath&URL)
     *
     * @param location    文件路径，支持http路径
     * @param charsetName
     * @return
     * @throws IOException
     */
    public static String loadFileContent(String location, String charsetName) throws IOException {
        String content = "";
        if (isHttpUri(location)) {
            try {
                URL url = new URL(location);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), charsetName));
                String line;
                while ((line = reader.readLine()) != null) {
                    content += line;
                }
                reader.close();
            } catch (IOException ex) {
                throw ex;
            }
        } else {
            try {
                content = FileUtils.fileRead(location, charsetName);
            } catch (IOException ex) {
                throw ex;
            }
        }
        return content;
    }

    /**
     * 加载java程序中的资源文件
     *
     * @param path
     * @param charsetName
     * @return
     * @throws IOException
     */
    public static String loadResourceFileContent(String path, String charsetName) throws IOException {
        InputStream in = SourceFileUtils.class.getClassLoader().getResourceAsStream(path);
        InputStreamReader reader = new InputStreamReader(in, charsetName);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        bufferedReader.lines().forEachOrdered(line -> {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

    public static boolean isHttpUri(String location){
        if(location==null){
            return false;
        }
        String lowerCaseLocation = location.toLowerCase();
        return lowerCaseLocation.startsWith("http://") || lowerCaseLocation.startsWith("https://");
    }

    /**
     * 判断是否绝对路径(支持FilePath&URL)
     *
     * @param location FilePath&URL
     * @return
     */
    public static boolean isAbsolutePathOrHttpUri(String location) {
        if (isHttpUri(location)) {
            return true;
        }
        if (File.separator.equals("/")) {
            return location.startsWith("/");
        } else {
            return location.length() > 3 && location.charAt(1) == ':' && location.charAt(2) == '\\';
        }
    }

    /**
     * 拼接路径(支持FilePath&URL)
     *
     * @param path1 基础路径 FilePath&URL
     * @param path2 待拼接目录
     * @return
     */
    public static String concatPathOrHttpUri(String path1, String path2) {
        if (isHttpUri(path1)) {
            return path1 + (path1.endsWith("/") ? "" : "/") + path2;
        } else if (File.separator.equals("\\")) {
            return path1 + (path1.endsWith(File.separator) ? "" : File.separator) + path2.replace("/", "\\");
        } else {
            return path1 + (path1.endsWith(File.separator) ? "" : File.separator) + path2;
        }
    }

    /**
     * 解析路径(支持FilePath&URL)
     *
     * @param location FilePath&URL
     * @return
     */
    public static String resolveDirectory(String location) {
        if (isHttpUri(location)) {
            if (location.endsWith("/")) {
                return location;
            } else {
                return location.substring(0, location.lastIndexOf("/") + 1);
            }
        } else {
            // 判断路径是否目录
            Path path = Paths.get(location);
            if (!Files.exists(path)) {
                throw new RuntimeException("路径不存在：" + location);
            }
            if (Files.isDirectory(path)) {
                return path.toAbsolutePath().toString() + File.separator;
            } else {
                return path.getParent().toAbsolutePath().toString() + File.separator;
            }
        }
    }

    /**
     * 解析java包在文件系统中的文件夹路径
     *
     * @param baseDir     java项目gen目录
     * @param packageName 包全路径
     * @return
     */
    public static String resolveDirectory(String baseDir, String packageName) {
        String dir = null;
        try {
            dir = new File(baseDir).getCanonicalPath() + File.separator + (SRC_MAIN_JAVA + packageName).replace(PACKAGE_SPLITTER, File.separator);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

    /**
     * 解析java类源码文件路径
     *
     * @param baseDir
     * @param packageName
     * @param className
     * @return
     */
    public static String resolveSourceFile(String baseDir, String packageName, String className) {
        String file = null;
        try {
            file = new File(baseDir).getCanonicalPath() + File.separator + (SRC_MAIN_JAVA + packageName).replace(PACKAGE_SPLITTER, File.separator) + File.separator + className + ".java";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 根据类名查找java源码文件
     *
     * @param baseDir
     * @param simpleClassName
     * @return
     */
    public static Optional<File> findJavaFileBySimpleClassName(String baseDir, String simpleClassName) {
        return loadFiles(baseDir).stream().filter(file -> resolveSimpleClassName(file.getAbsolutePath()).equalsIgnoreCase(simpleClassName)).findFirst();
    }

    /**
     * 解析java类名
     *
     * @param filePath java源码文件路径
     * @return
     */
    public static String resolveClassName(String filePath) {
        if (!filePath.endsWith(".java")) {
            throw new RuntimeException("非java源码文件路径无法解析类名");
        }
        String className = filePath.replace(File.separator, PACKAGE_SPLITTER).replaceAll("\\.java$", "");
        int idx = -1;

        if (className.lastIndexOf(SRC_MAIN_JAVA) >= 0) {
            idx = className.lastIndexOf(SRC_MAIN_JAVA) + SRC_MAIN_JAVA.length();
        } else if (className.lastIndexOf(SRC_TEST_JAVA) >= 0) {
            idx = className.lastIndexOf(SRC_TEST_JAVA) + SRC_TEST_JAVA.length();
        } else {
            return "";
        }
        className = className.substring(idx);
        return className;
    }

    /**
     * 拼接包名
     *
     * @param packages
     * @return
     */
    public static String concatPackage(String... packages) {
        return packages == null || packages.length == 0
                ? ""
                : Arrays.stream(packages).reduce((a, b) -> StringUtils.isBlank(a) || StringUtils.isBlank(b) || b.startsWith(PACKAGE_SPLITTER) ? (a + b) : (a + PACKAGE_SPLITTER + b)).orElse("");
    }

    /**
     * 计算相对包名
     *
     * @param fullPackage
     * @param basePackage
     * @return 结果返回保留最前面的包分隔符'.'。比如 com.abc.x 相对于 com.abc 的相对包名为 .x
     */
    public static String refPackage(String fullPackage, String basePackage) {
        if (null == fullPackage || null == basePackage || !fullPackage.startsWith(basePackage)) {
            throw new RuntimeException("无法计算相对包路径");
        }
        return fullPackage.substring(basePackage.length());
    }

    /**
     * 标准化相对包名 以包分隔符 '.' 开头
     *
     * @param refPackage
     * @return
     */
    public static String refPackage(String refPackage) {
        if (StringUtils.isBlank(refPackage)) {
            return "";
        }
        if (!refPackage.startsWith(PACKAGE_SPLITTER)) {
            return PACKAGE_SPLITTER + refPackage;
        }
        return refPackage;
    }

    /**
     * 解析包名
     *
     * @param filePath java源码文件路径
     * @return
     */
    public static String resolvePackage(String filePath) {
        String className = resolveClassName(filePath);
        String packageName = className.substring(0, className.lastIndexOf(PACKAGE_SPLITTER));
        return packageName;
    }

    /**
     * 解析类名，不含包路径
     *
     * @param filePath java源码文件路径
     * @return
     */
    public static String resolveSimpleClassName(String filePath) {
        String className = resolveClassName(filePath);
        String simpleClassName = className.substring(className.lastIndexOf(PACKAGE_SPLITTER) + 1);
        return simpleClassName;
    }

    /**
     * 默认包名，从项目根目录下找到第一个java源码文件，解析包名，默认取前三级包名
     *
     * @param baseDir
     * @return
     */
    public static String resolveDefaultBasePackage(String baseDir) {
        try {
            Optional<File> javaFile = loadFiles
                    (
                            new File(baseDir).getCanonicalPath() + File.separator + SRC_MAIN_JAVA.replace(PACKAGE_SPLITTER, File.separator)
                    )
                    .stream()
                    .filter(
                            file -> FileUtils.getExtension(file.getAbsolutePath())
                                    .contains("java")
                    )
                    .findFirst();
            if (javaFile.isPresent()) {
                String packageName = resolvePackage(javaFile.get().getCanonicalPath());
                String[] packages = packageName.split("\\.");
                switch (packages.length) {
                    case 0:
                        throw new RuntimeException("解析默认basePackage失败");
                    case 1:
                        return packages[0];
                    case 2:
                        return packages[0] + PACKAGE_SPLITTER + packages[1];
                    default:
                        return packages[0] + PACKAGE_SPLITTER + packages[1] + PACKAGE_SPLITTER + packages[2];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("未找到java源文件");
    }


    public static void replaceText(List<String> list, String regex, String replace) {
        for (int i = 0; i < list.size(); i++) {
            String l = list.get(i);
            if (StringUtils.isBlank(l)) {
                continue;
            }
            String n = l.replaceAll(regex, replace);
            if (StringUtils.isBlank(n)) {
                list.remove(i);
                i--;
            } else {
                list.remove(i);
                list.add(i, n);
            }
        }
    }

    public static void removeText(List<String> list, String regex) {
        replaceText(list, regex, "");
    }

    public static void distinctText(List<String> list, String regex) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String l = list.get(i);
            if (l.matches(regex)) {
                indexes.add(i);
            }
        }
        for (int i = 1; i < indexes.size(); i++) {
            String l = list.get(indexes.get(i));
            list.remove(indexes.get(i).intValue());
            String n = l.replaceAll(regex, "");
            if (StringUtils.isNotBlank(n)) {
                list.add(indexes.get(i), n);
            }
        }
    }

    public static boolean hasLine(List<String> list, String regex) {
        for (String l : list) {
            if (l.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public static void addIfNone(List<String> list, String regex, String line) {
        if (!hasLine(list, regex)) {
            list.add(line);
        }
    }

    public static void addIfNone(List<String> list, String regex, String line, BiFunction<List<String>, String, Integer> idx) {
        if (!hasLine(list, regex)) {
            list.add(idx.apply(list, line).intValue(), line);
        }
    }

    public static void writeLine(BufferedWriter out, String line) {
        try {
            out.write(line);
            out.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
