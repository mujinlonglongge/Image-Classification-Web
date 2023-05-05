package zhao.core.model;

import org.apache.commons.io.IOUtils;
import zhao.Conf;
import zhao.algorithmMagic.utils.transformation.Transformation;
import zhao.core.user.OrdinaryUser;
import zhao.core.user.User;
import zhao.utils.ExeUtils;
import zhao.utils.HttpUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * 训练服务类，在数据上传成功之后将会跳转到该类对应的门户页面
 */
@WebServlet(name = "TrainServlet", value = "/TrainServlet")
public class TrainServlet extends HttpServlet {

    /**
     * 用户自定义训练目录 的 训练函数实现对象，该对象将会按照用户的数据集进行训练操作。
     */
    public final static Transformation<User, InputStream> TRAIN_UD_DATA = user -> {
        final String s = user.getTrainDir() + " " + user.getModelDir() + "/Model ";
        final String s1 = user.getModelDir();
        try {
            return ExeUtils.exePy(
                    Conf.TRAIN_PYTHON_PATH,
                    s +
                            s1 + "/tempClassList.txt" + ' ' +
                            s1 + "/classList.txt"
            );
        } catch (IOException e) {
            return null;
        }
    };

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final User user = HttpUtils.intiCheck(request, response);
        if (user.equals(OrdinaryUser.DEFAULT_USER)) {
            // 若是 def 代表当前用户没有登录 直接结束
            return;
        }
        // 将临时的类别文件保存到新文件中
        final PrintWriter writer = response.getWriter();
        trainReturn(user, writer, TRAIN_UD_DATA);
    }

    /**
     * 训练同时返回训练结果页面的函数实现类型
     *
     * @param user           当前需要训练数据的用户对象
     * @param writer         返回结果页面的数据流
     * @param transformation 训练脚本处理类
     * @throws IOException 异常
     */
    protected void trainReturn(User user, PrintWriter writer, Transformation<User, InputStream> transformation) throws IOException {
        writer.write("<!DOCTYPE html>");
        writer.write(" <html lang=\"zh\">");
        writer.write(" <head>");
        writer.write(" <meta charset=\"GBK\">");
        writer.write(" <title>训练结果</title>");
        writer.write(" </head>");
        writer.write("<h2>训练日志展示</h2>\n");
        writer.write("<hr>\n");
        writer.write("<pre>");
        // 调用demo.py中的method1方法
        InputStream inputStream = transformation.function(user);
        if (inputStream != null) {
            IOUtils.copy(inputStream, writer, "GBK");
            writer.write("</pre>");
            writer.write("<hr>\n");
            writer.println("<p>模型已经保存至您的个人目录中，您可以随时进行模型的使用。</p>");
            writer.println("<a href = " + Conf.USE_MODEL_HTML + "> 点击使用模型 </a><br>");
        } else {
            writer.write("<p>模型训练出现错误!!!!!</p>");
        }
        writer.println("<a href = " + Conf.HOME + "> 点击回到主页 </a><br>");
    }
}
