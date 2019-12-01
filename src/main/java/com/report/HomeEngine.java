package com.report;
import com.report.swing.BeautyEyeLNFHelper;
import org.jb2011.lnf.beautyeye.ch3_button.BEButtonUI;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志生成工具图形界面
 * @author Charles Wesley
 * @date 2019/12/1 14:47
 */
public class HomeEngine extends JFrame {
    public static void main(String[] args) {
        try {
            BeautyEyeLNFHelper.launchBeautyEyeLNF();
            //隐藏设置按钮
            UIManager.put("RootPane.setupButtonVisible",false);
            //BeautyEyeLNFHelper.translucencyAtFrameInactive = false;
            //让Swing的样式变得和当前系统一致
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // 调整默认字体
//            for (int i = 0; i < FontUtil.DEFAULT_FONT.length; i++){
//                UIManager.put(FontUtil.DEFAULT_FONT[i],new Font("微软雅黑", Font.PLAIN,14));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HomeEngine test = new HomeEngine();
        test.forms();
    }

    public void forms() {
        Font font = new Font("微软雅黑", Font.PLAIN, 14);
        //设置顶部提示文字和主窗体的宽，高，x值，y值   其中x和y对应组件在母窗体中出现的坐标
        setTitle("周报填写工具");
        setBounds(600, 400, 580, 480);

        JPanel jPanel = new JPanel();
        jPanel.setBounds(1, 1, 580, 480);
        jPanel.setLayout(null);
        //添加一个cp容器
        Container cp = getContentPane();
        //设置添加的cp容器为绝对布局
        cp.setLayout(null);

        //设置左侧用户名文字
        JLabel jl = new JLabel("用户名：");
        jl.setFont(font);
        jl.setBounds(10, 10, 400, 50);
        //用户名框
        final JTextField name = new JTextField();
        //设置用户名框的宽，高，x值，y值
        name.setBounds(80, 10, 350, 50);

        //设置左侧密码文字
        JLabel jl2 = new JLabel("密码：");
        jl2.setFont(font);
        jl2.setBounds(10, 100, 400, 50);
        //密码框：为加密的***
        final JPasswordField password = new JPasswordField();
        //设置密码框的宽，高，x值，y值
        password.setBounds(80, 100, 350, 50);

        JLabel jl3 = new JLabel("日期：");
        jl3.setFont(font);
        jl3.setBounds(10, 190, 400, 50);
        //日期输入框
        final JTextField dateField = new JTextField();
        dateField.setBounds(80, 190, 350, 50);

        //将jl、name、jl2、password添加到容器cp中
        cp.add(jl);
        cp.add(name);
        cp.add(jl2);
        cp.add(password);
        cp.add(jl3);
        cp.add(dateField);

        //确定按钮
        //添加一个确定按钮
        JButton jb = new JButton("确定");
        //为确定按钮添加监听事件
        jb.addActionListener(arg0 -> {
            if (name.getText().trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "用户名不允许为空");
                return;
            }

            if(new String(password.getPassword()).trim().length() == 0){
                JOptionPane.showMessageDialog(null, "密码不允许为空");
                return;
            }

            LocalDateTime targetTime = LocalDateTime.now();
            if (dateField.getText().trim().length() != 0) {
                targetTime = LocalDateTime.parse(dateField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            try{
                ReportEngine.generator(name.getText().trim(), new String(password.getPassword()).trim(), targetTime);
                JOptionPane.showMessageDialog(null, "生成完毕");
            } catch (Exception e){
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "生成失败，请联系管理员");
            }

        });
        //设置确定按钮的宽，高，x值，y值
        jb.setBounds(80, 300, 150, 40);
        //设置按钮的颜色为绿色
        jb.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
        //将确定按钮添加到cp容器中
        cp.add(jb);

        //重置按钮
        final JButton button = new JButton();
        button.setText("重置");
        //为重置按钮添加监听事件
        //同时清空name、password的数据
        button.addActionListener(arg0 -> {
            name.setText("");
            password.setText("");
        });
        //设置重置按钮的宽，高，x值，y值
        button.setBounds(280, 300, 150, 40);
        //设置按钮的颜色为红色
        button.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
        getContentPane().add(button);

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //jdk1.4后提供，null表示窗口居于屏幕的中央
        setLocationRelativeTo(null);

        jPanel.add(cp);
        setContentPane(jPanel);
        setResizable(false);
    }
}


