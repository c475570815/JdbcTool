package indi.cyh.jdbctool.tool;

import indi.cyh.jdbctool.config.ConfigCenter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description TODO * @Author CYH * @Date 2021/9/17 16:24
 **/
public class FileUtils {
    /**
     * 获取windows/linux的项目根目录
     *
     * @return
     */
    public static String getConTextPath() {
        String fileUrl = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(ConfigCenter.configFileName)).getPath();
        String jarName = getJarName(fileUrl);
        if (StringTool.isEmpty(jarName)) {
            return new File(fileUrl).getParentFile().getPath();
        } else {
            if (fileUrl.indexOf("file:") == 0) {
                fileUrl = fileUrl.replaceFirst("file:", "");
            }
            return fileUrl.substring(0, fileUrl.indexOf(jarName));
        }
    }

    public static boolean isWidows() {
        return System.getProperties().getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getJarName(String fileFullPath) {
        Matcher m = Pattern.compile("([^<>/\\\\\\|:\"\"\\*\\?]+\\.jar)").matcher(fileFullPath);
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }

    }

    /**
     * 读取文件
     *
     * @param Path
     * @return
     */
    public static String ReadFile(String Path) {
        BufferedReader reader = null;
        String laststr = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(Path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr;
    }

    /**
     * 获取文件夹下所有文件的名称 + 模糊查询(当不需要模糊查询时，queryStr传空或null即可)
     * <p>
     * 1.当路径不存在时，map返回retType值为1
     * <p>
     * 2.当路径为文件路径时，map返回retType值为2，文件名fileName值为文件名
     * <p>
     * 3.当路径下有文件夹时，map返回retType值为3，文件名列表fileNameList，文件夹名列表folderNameList
     *
     * @param folderPath 路径
     * @param queryStr   模糊查询字符串
     * @return
     */
    public static HashMap getFilesName(String folderPath, String queryStr) {
        HashMap map = new HashMap<>();
        List fileNameList = new ArrayList<>();
        //文件名列表
        List folderNameList = new ArrayList<>();
        //文件夹名列表
        File f = new File(folderPath);
        if (!f.exists()) {
            //路径不存在
            map.put("retType", "1");
        } else {
            boolean flag = f.isDirectory();
            if (flag == false) {
                //路径为文件
                map.put("retType", "2");
                map.put("fileName", f.getName());
            } else {
                //路径为文件夹
                map.put("retType", "3");
                File fa[] = f.listFiles();
                queryStr = queryStr == null ? "" : queryStr;
                //若queryStr传入为null,则替换为空(indexOf匹配值不能为null)
                for (int i = 0; i < fa.length; i++) {
                    File fs = fa[i];
                    if (fs.getName().contains(queryStr)) {
                        if (fs.isDirectory()) {
                            folderNameList.add(fs.getName());
                        } else {
                            fileNameList.add(fs.getName());
                        }
                    }
                }
                map.put("fileNameList", fileNameList);
                map.put("folderNameList", folderNameList);
            }
        }
        return map;
    }

    /**
     * 以行为单位读取文件，读取到最后一行
     *
     * @param filePath
     * @return
     */
    public static List readFileContent(String filePath) {
        BufferedReader reader = null;
        List listContent = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                listContent.add(tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return listContent;
    }

    /**
     * 读取指定行数据 ，注意：0为开始行
     *
     * @param filePath
     * @param lineNumber
     * @return
     */
    public static String readLineContent(String filePath, int lineNumber) {
        BufferedReader reader = null;
        String lineContent = "";
        try {
            reader = new BufferedReader(new FileReader(filePath));
            int line = 0;
            while (line <= lineNumber) {
                lineContent = reader.readLine();
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return lineContent;
    }

    /**
     * 读取从beginLine到endLine数据(包含beginLine和endLine)，注意：0为开始行
     *
     * @param filePath
     * @param beginLineNumber 开始行
     * @param endLineNumber   结束行
     * @return
     */
    public static List readLinesContent(String filePath, int beginLineNumber, int endLineNumber) {
        List listContent = new ArrayList<>();
        try {
            int count = 0;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String content = reader.readLine();
            while (content != null) {
                if (count >= beginLineNumber && count <= endLineNumber) {
                    listContent.add(content);
                }
                content = reader.readLine();
                count++;
            }
        } catch (Exception e) {
        }
        return listContent;
    }

    /**
     * 读取若干文件中所有数据
     *
     * @param listFilePath
     * @return
     */
    public static List readFileContent_list(List<String> listFilePath) {
        List listContent = new ArrayList<>();
        for (String filePath : listFilePath) {
            File file = new File(filePath);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                int line = 1;
                // 一次读入一行，直到读入null为文件结束
                while ((tempString = reader.readLine()) != null) {
                    listContent.add(tempString);
                    line++;
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }
        return listContent;
    }

    /**
     * 文件数据写入(如果文件夹和文件不存在，则先创建，再写入)
     *
     * @param filePath
     * @param content
     * @param flag     true:如果文件存在且存在内容，则内容换行追加；false:如果文件存在且存在内容，则内容替换
     */
    public static String fileLinesWrite(String filePath, String content, boolean flag) {
        String filedo = "write";
        FileWriter fw = null;
        try {
            File file = new File(filePath);
            //如果文件夹不存在，则创建文件夹
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                //如果文件不存在，则创建文件,写入第一行内容
                file.createNewFile();
                fw = new FileWriter(file);
                filedo = "create";
            } else {//如果文件存在,则追加或替换内容
                fw = new FileWriter(file, flag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println(content);
        pw.flush();
        try {
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filedo;
    }

    /**
     * 写文件
     *
     * @param ins
     * @param out
     */
    public static void writeIntoOut(InputStream ins, OutputStream out) {
        byte[] bb = new byte[10 * 1024];
        try {
            int cnt = ins.read(bb);
            while (cnt > 0) {
                out.write(bb, 0, cnt);
                cnt = ins.read(bb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.flush();
                ins.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建目录
     *
     * @param dir_path
     */
    public static void mkDir(String dir_path) {
        File myFolderPath = new File(dir_path);
        try {
            if (!myFolderPath.exists()) {
                myFolderPath.mkdir();
            }
        } catch (Exception e) {
            System.out.println("新建目录操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 判断指定的文件是否存在。
     *
     * @param fileName
     * @return
     */
    public static boolean isFileExist(String fileName) {
        return new File(fileName).isFile();
    }

    /**
     * 得到文件后缀名
     *
     * @param fileName
     * @return
     */
    public static String getFileExt(String fileName) {
        int point = fileName.lastIndexOf('.');
        int length = fileName.length();
        if (point == -1 || point == length - 1) {
            return "";
        } else {
            return fileName.substring(point + 1, length);
        }
    }

}
