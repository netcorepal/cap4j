package org.netcorepal.cap4j.ddd.codegen.misc;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author binking338
 * @date 2022-02-17
 */
public class SourceFileUtils {

    private static Map<String, List<File>> Cache = new HashMap<>();
    private final static String JAVA_SRC_DIRS = "src.main.java.";

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

    public static String loadFileContent(String location) throws IOException {
        String content = "";
        if (location.startsWith("http://") || location.startsWith("https://")) {
            try {
                URL url = new URL(location);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    content += line;
                }
                reader.close();
            } catch (IOException ex){
                throw ex;
            }
        } else {
            try {
                content = new String(Files.readAllBytes(Paths.get(location)));
            } catch (IOException ex){
                throw ex;
            }
        }
        return content;
    }

    public static String loadResourceFileContent(String path) throws IOException {
        InputStream in = SourceFileUtils.class.getClassLoader().getResourceAsStream(path);
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        bufferedReader.lines().forEachOrdered(line -> {
            stringBuilder.append(line);
            stringBuilder.append(LineSeparator.Unix);
        });
        return stringBuilder.toString();
    }

    public static String resolveDirectory(String baseDir, String packageName) {
        String dir = null;
        try {
            dir = new File(baseDir).getCanonicalPath() + File.separator + (JAVA_SRC_DIRS + packageName).replace(".", File.separator);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

    public static String resolveSourceFile(String baseDir, String packageName, String className){
        String file = null;
        try {
            file = new File(baseDir).getCanonicalPath() + File.separator + (JAVA_SRC_DIRS + packageName).replace(".", File.separator) + File.separator + className + ".java";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static Optional<File> findJavaFileBySimpleClassName(String baseDir, String simpleClassName) {
        return loadFiles(baseDir).stream().filter(file -> resolveSimpleClassName(file.getAbsolutePath()).equalsIgnoreCase(simpleClassName)).findFirst();
    }

    public static String resolveClassName(String filePath) {
        String className = filePath.replace(File.separator, ".").replaceAll("\\.java$", "");
        className = className.substring(className.lastIndexOf(JAVA_SRC_DIRS) + JAVA_SRC_DIRS.length());
        return className;
    }

    public static String resolvePackage(String filePath) {
        String className = resolveClassName(filePath);
        String packageName = className.substring(0, className.lastIndexOf("."));
        return packageName;
    }

    public static String resolveSimpleClassName(String filePath) {
        String className = resolveClassName(filePath);
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        return simpleClassName;
    }

    public static String resolveBasePackage(String baseDir) {
        try {
            Optional<File> javaFile = loadFiles(new File(baseDir).getCanonicalPath() + File.separator + JAVA_SRC_DIRS.replace(".", File.separator))
                    .stream().filter(file -> FileUtils.getExtension(file.getAbsolutePath()).contains("java")).findFirst();
            if (javaFile.isPresent()) {
                String packageName = resolvePackage(javaFile.get().getCanonicalPath());
                String[] packages = packageName.split("\\.");
                return packages[0] + "." + packages[1] + "." + packages[2];
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

    public static void addSortedIfNone(List<String> list, String regex, String line) {
        if (!hasLine(list, regex)) {
            Optional<String> first = list.stream().filter(l -> l.compareToIgnoreCase(line) > 0).findFirst();
            if (first.isPresent()) {
                list.add(list.indexOf(first.get()), line);
            } else {
                list.add(0, line);
            }
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
