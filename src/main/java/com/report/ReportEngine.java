package com.report;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.report.pojo.LogPO;
import com.report.utils.DateUtil;
import com.report.utils.FileUtil;
import com.report.utils.HttpUtil;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.*;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Charles Wesley
 * @date 2019/11/30 16:38
 */
public class ReportEngine {
    /**
     * BOSS官网登陆时使用的验证码图片持久化到本地的路径
     */
    final static private String SECURITY_CODE_FILE_SAVE_PATH = System.getProperties().getProperty("user.home") + File.separator + "security_code.jpg";

    /**
     * 周报模板存储路径 仅供测试时使用
     */
    final static private String WEEK_REPORT_TEMPLATE_FILE_SAVE_PATH = "D://report_template.xlsx";

    /**
     * 周报模板文件名称
     */
    final static private String WEEK_REPORT_TEMPLATE_FILE_NAME = "report_template.xlsx";

    public static void main(String[] args) {
        generator("mamr", "mamingrui940810#", LocalDateTime.now());
    }

    public static boolean generator(String username, String password, LocalDateTime targetTime){
        long currentMills = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();

        //1. 获取erpsessionid
        String erpsessionid = null;
        Map<String, Object> erpMap = HttpUtil.doGet("https://www.uyun.cn/boss/opercentre/user/viewselfinfo",
                new HashMap<String, String>(1){
                    private static final long serialVersionUID = 6389902712215569342L;
                    {put(null, "" + currentMills);}}, null);
        if(erpMap != null){
            Header[] erpHeader =  (Header[])erpMap.get("header");
            for(Header header : erpHeader){
                if("Set-Cookie".equals(header.getName())){
                    HeaderElement[] elements = header.getElements();
                    for(HeaderElement element : elements){
                        if("erpsessionid".equals(element.getName())){
                            erpsessionid = element.getValue();
                        }
                    }
                }
            }
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/x-www-form-urlencoded");
        headerMap.put("Cookie", "erpsessionid=" + erpsessionid);

        String securityCode = null;

        int securityCodeRepeatCount = 0;
        while(true){
            //2. 获取验证码图片并下载到本地 有一定概率识别出错，需要进行正则匹配，若识别后的验证码中含有非数字，则对Step2和Step3进行重试
            byte[] securityCodeBytes = null;
            try {
                securityCodeBytes = HttpUtil.doGetBytes("https://www.uyun.cn/boss/resources/enimg.jsp",
                        new HashMap<String, String>(1){
                            private static final long serialVersionUID = 1450374993383400609L;
                            {put(null, "" + currentMills);}}, headerMap);
            } catch (Exception e) {
                System.out.println("获取验证码图片失败，原因: " + e.toString());
            }

            File securityCodeFle = null;
            if(securityCodeBytes != null){
                try {
                    //将图片转存至当前用户目录下
                    FileUtil.byteToFile(securityCodeBytes, SECURITY_CODE_FILE_SAVE_PATH);
                    securityCodeFle = new File(SECURITY_CODE_FILE_SAVE_PATH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //3. 读取并识别验证码的内容
            if(securityCodeFle != null){
                Tesseract instance = new Tesseract();
                File tessDataFolder = LoadLibs.extractTessResources("tessdata");
                //英文库识别数字的准确度更高
                instance.setLanguage("eng");
                instance.setDatapath(tessDataFolder.getAbsolutePath());
                try {
                    securityCode = instance.doOCR(securityCodeFle).replace("\n", "");
                    System.out.println("验证码: " + securityCode);
                }catch (TesseractException e){
                    System.out.println("图片识别出错，原因: " + e.toString());
                }
            }
            if(securityCode != null && securityCode.matches("^[0-9]*$")){
                break;
            }

            securityCodeRepeatCount++;
            if(securityCodeRepeatCount > 5){
                break;
            }
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //4. 带着验证码、用户名、密码模拟登陆BOSS官网
        if(securityCode != null){
            Map<String, String> loginMap = new HashMap<>();
            loginMap.put("rand", securityCode);
            loginMap.put("operUserName", username);
            loginMap.put("passWord", password);

            String loginResult = HttpUtil.doPost("https://www.uyun.cn/boss/opercentre/loginaction/checklogin", loginMap, headerMap);
            System.out.println("登陆结果: " + loginResult);
        }

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //5. 查询本周内的日志，以项目taskId来分组
        Map<String, Date> dayWeeks = DateUtil.getDayWeeks(DateUtil.localDateTime2Date(targetTime));
        Map<String, String> logParamMap = new HashMap<>();
        logParamMap.put("pageNo", "1");
        logParamMap.put("pageSize", "10");
        logParamMap.put("orderby", "");
        logParamMap.put("sortField", "");
        logParamMap.put("startTime", "" + dayWeeks.get("Monday").getTime());
        logParamMap.put("endTime", "" + dayWeeks.get("Sunday").getTime());

        Map<String, List<LogPO>> logPoMap = new HashMap<>();
        int logRepeatCount = 0;
        while(true){
            Map<String, Object> logResultMap = HttpUtil.doGet("https://www.uyun.cn/boss/opercentre/crm/task/listAllLog", logParamMap, headerMap);
            if(logResultMap != null){
                JSONObject logResultBody = JSONObject.parseObject((String) logResultMap.get("body"));
                if(!"401".equals(logResultBody.getString("errCode")) && logResultBody.getInteger("total") > 0){
                    List<LogPO> logPoList = JSONArray.parseArray(logResultBody.getString("records"), LogPO.class);
                    //注意typeName为请假的日志不要录入到报表中
                    logPoMap.putAll(logPoList.stream().filter(e->e.getTaskId() != null && !"".equals(e.getTaskId())).collect(Collectors.groupingBy(LogPO::getTaskId)));
                    break;
                }else{
                    logRepeatCount++;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("Log线程睡眠异常，原因: " + e.toString());
                    }
                }
                if(logRepeatCount > 5){
                    break;
                }
            }
        }

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //6. 获取项目编号
        List<String> taskIds = new ArrayList<>(logPoMap.keySet());
        //获取所有未完成的任务
        Map<String, String> taskParamMap = new HashMap<>();
        taskParamMap.put("pageNo","" + 1);
        taskParamMap.put("pageSize", "" + Integer.MAX_VALUE);
        taskParamMap.put("filterKey","" + 2);
        //taskId : 项目编号
        Map<String, String> taskMap = new HashMap<>();

        int taskRepeatCount = 0;
        while(true){
            String taskResult = HttpUtil.doPost("https://www.uyun.cn/boss/opercentre/crm/task/listAll", taskParamMap, headerMap);
            if(taskResult != null && !"".equals(taskResult)){
                int repeatCount = 0;
                JSONObject object = JSONObject.parseObject(taskResult);
                if(!"401".equals(object.getString("errCode")) && object.getInteger("total") > 0){
                    JSONArray jsonArray = JSONArray.parseArray(object.getString("records"));
                    if(jsonArray != null){
                        for(int i = 0; i< jsonArray.size(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if(jsonObject != null && taskIds.contains(jsonObject.getString("taskId"))){
                                taskMap.putIfAbsent(jsonObject.getString("taskId"), jsonObject.getString("projectNo"));
                            }
                        }
                    }
                    break;
                }else{
                    taskRepeatCount++;
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("task线程睡眠异常，原因: " + e.toString());
                    }
                }
                if(taskRepeatCount > 5){
                    break;
                }
            }
        }

        //7. 读取jar包中的周报填报模板，导出到桌面上
        InputStream inputStream = FileUtil.readJarFile("file" + "/" + WEEK_REPORT_TEMPLATE_FILE_NAME);

        File reportTemplateFile = new File(WEEK_REPORT_TEMPLATE_FILE_SAVE_PATH);
        LocalDateTime friday = DateUtil.date2LocalDateTime(dayWeeks.get("Monday")).plusDays(4);

        //最终生成的周报的路径
        String reportPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath().concat(File.separator).concat(DateTimeFormatter.ofPattern("yyyyMMdd").format(friday)).concat("周报.xlsx");

        //复制模板，生成样本
        File reportFile = null;
        try {
            reportFile = FileUtil.asFile(inputStream, reportPath);
            //FileUtil.copyFileForFileStreams(reportTemplateFile, reportFile);
        } catch (IOException e) {
            System.out.println("复制模板报错，原因: " + e.toString());
        }

        try {
            if(reportFile != null){
                fillExcel(reportFile, logPoMap, taskMap, targetTime);
            }
        } catch (IOException | InvalidFormatException e) {
            System.out.println("填报模板报错，原因: " + e.toString());
        }

        return true;
    }

    private static void fillExcel(File reportFile, Map<String, List<LogPO>> logPoMap, Map<String, String> taskMap, LocalDateTime targetTime) throws IOException, InvalidFormatException {
        Map<String, Date> dayWeeks = DateUtil.getDayWeeks(DateUtil.localDateTime2Date(targetTime));
        LocalDateTime friday = DateUtil.date2LocalDateTime(dayWeeks.get("Monday")).plusDays(4);
        //数据填报
        try (XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(reportFile)); OutputStream out = new FileOutputStream(reportFile)) {
            XSSFFont font = workbook.createFont();
            font.setFontName("微软雅黑");
            font.setFontHeightInPoints((short)11);
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet != null && "开发任务情况".equals(sheet.getSheetName())) {
                    //行号本身从0开始，由于第一行是说明行，因此略过，我们从第二行开始填报
                    int firstRowNum = 1;

                    //以日志的任务id来分组，每一组中所有的任务都填在同一行
                    for (Map.Entry<String, List<LogPO>> entry : logPoMap.entrySet()) {
                        XSSFCellStyle cellStyle = workbook.createCellStyle();
                        //设置自动换行
                        cellStyle.setWrapText(true);
                        //设置字体
                        cellStyle.setFont(font);
                        //设置cell所引用的样式是否锁住
                        //cellStyle.setLocked(true);
                        XSSFRow row = sheet.createRow(firstRowNum);
                        //必须使用setHeightInPoints(),row.setHeight()不起作用
                        row.setHeightInPoints(Short.parseShort(28 * entry.getValue().size() + ""));
                        StringBuffer sb = new StringBuffer();
                        IntStream.range(0, entry.getValue().size()).forEach(i -> {
                            sb.append(i+1).append(". ").append(entry.getValue().get(i).getRemark()).append("\n");
                        });
                        //删掉最后一个换行符

                        //同一批任务的属性大部分相同，因此直接取第一个任务即可
                        LogPO logPo = entry.getValue().get(0);
                        //序号
                        XSSFCell cell0 = row.createCell(0);
                        cell0.setCellValue(firstRowNum);
                        cell0.setCellStyle(cellStyle);
                        //项目编号
                        XSSFCell cell1 = row.createCell(1);
                        cell1.setCellValue(taskMap.get(logPo.getTaskId()));
                        cell1.setCellStyle(cellStyle);
                        //项目名称
                        XSSFCell cell2 = row.createCell(2);
                        cell2.setCellValue(logPo.getProjectName());
                        cell2.setCellStyle(cellStyle);
                        //任务名称
                        XSSFCell cell3 = row.createCell(3);
                        cell3.setCellValue(sb.substring(0, sb.length()-2));
                        cell3.setCellStyle(cellStyle);
                        //预计完成时间
                        XSSFCell cell4 = row.createCell(4);
                        cell4.setCellValue(DateTimeFormatter.ofPattern("yyyy/MM/dd").format(friday));
                        cell4.setCellType(CellType.STRING);
                        cell4.setCellStyle(cellStyle);
                        //任务性质
                        XSSFCell cell5 = row.createCell(5);
                        cell5.setCellValue("合同内");
                        cell5.setCellStyle(cellStyle);
                        //任务执行情况
                        XSSFCell cell6 = row.createCell(6);
                        cell6.setCellValue("已经完成");
                        cell6.setCellStyle(cellStyle);
                        //未启动与停滞原因
                        XSSFCell cell7 = row.createCell(7);
                        cell7.setCellValue("");
                        cell7.setCellStyle(cellStyle);
                        //本任务剩余工作量
                        XSSFCell cell8 = row.createCell(8);
                        cell8.setCellValue("");
                        cell8.setCellStyle(cellStyle);
                        //限定截止日
                        XSSFCell cell9 = row.createCell(9);
                        cell9.setCellValue(DateTimeFormatter.ofPattern("yyyy/MM/dd").format(friday));
                        cell9.setCellType(CellType.STRING);
                        cell9.setCellStyle(cellStyle);
                        //已投入人力
                        XSSFCell cell10 = row.createCell(10);
                        cell10.setCellValue(logPo.getCreateUserName());
                        cell10.setCellStyle(cellStyle);
                        //开发负责人
                        XSSFCell cell11 = row.createCell(11);
                        cell11.setCellValue(logPo.getCreateUserName());
                        cell11.setCellStyle(cellStyle);
                        //整体完成时间点
                        XSSFCell cell12 = row.createCell(12);
                        cell12.setCellValue(DateTimeFormatter.ofPattern("yyyy/MM/dd").format(friday));
                        cell12.setCellType(CellType.STRING);
                        cell12.setCellStyle(cellStyle);
                        //本周跟进情况
                        XSSFCell cell13 = row.createCell(13);
                        cell13.setCellValue("已经完成");
                        cell13.setCellStyle(cellStyle);
                        //说明
                        XSSFCell cell14 = row.createCell(14);
                        cell14.setCellValue("");
                        cell14.setCellStyle(cellStyle);

                        firstRowNum++;
                    }
                }
            }

            workbook.write(out);
            System.out.println("填报完成");
        }
    }
}
