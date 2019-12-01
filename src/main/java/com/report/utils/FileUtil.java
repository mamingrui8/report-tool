package com.report.utils;

import java.io.*;

/**
 * 文件工具类
 * @author Charles Wesley
 * @date 2019/11/30 17:12
 */
public class FileUtil {
    public static File asFile(InputStream inputStream, String destinationPath) throws IOException{
        File file = new File(destinationPath);
        if(file.exists()){
            file.delete();
        }
        File tmp = new File(destinationPath);
        tmp.createNewFile();

        OutputStream os = new FileOutputStream(tmp);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return tmp;
    }

    /**
     *  读取jar包中的资源文件
     * @param fileName 文件名
     * @return 文件内容
     */
    public static InputStream readJarFile(String fileName){
        System.out.println("fileName: " + fileName);
        InputStream resourceAsStream = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
        System.out.println(resourceAsStream);
        return resourceAsStream;
    }
    /**
     * 文件复制
     * @param source 源文件
     * @param destination 目标文件
     * @throws IOException IO异常
     */
    public static void copyFileForFileStreams(File source, File destination)
            throws IOException {
        try (
                InputStream input = new FileInputStream(source);
                OutputStream output = new FileOutputStream(destination);
        ){
            //文件流暂存缓冲器
            byte[] buffer = new byte[1024];
            //每轮读取的字节数的数量
            int currentBuf;
            while((currentBuf = input.read(buffer)) != -1){
                output.write(buffer, 0, currentBuf);
            }
        }
    }

    /**
     * 把二进制数据转成指定后缀名的文件，例如PDF，PNG等
     * @param contents 二进制数据
     * @param filePath 文件存放目录，包括文件名及其后缀，如D:\file\bike.jpg
     */
    public static void byteToFile(byte[] contents, String filePath) throws Exception {
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream output = null;
        try {
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(contents);
            bis = new BufferedInputStream(byteInputStream);
            File file = new File(filePath);
            // 获取文件的父路径字符串
            File path = file.getParentFile();
            if (!path.exists()) {
                if (! path.mkdirs()) {
                    throw new Exception("创建文件夹失败，path="+path);
                }
            }
            fos = new FileOutputStream(file);
            // 实例化OutputString 对象
            output = new BufferedOutputStream(fos);
            byte[] buffer = new byte[1024];
            int length = bis.read(buffer);
            while (length != -1) {
                output.write(buffer, 0, length);
                length = bis.read(buffer);
            }
            output.flush();
        } catch (Exception e) {
            throw new Exception("输出文件流时抛异常，filePath=" + filePath + ", 原因: " + e.toString());
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e0) {
                e0.printStackTrace();
            }
        }
    }
}
