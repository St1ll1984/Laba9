import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {
    private TemplateEngine engine;
    private static final String COOKIE_NAME = "lastTimezone";
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        
        String timezoneParam = request.getParameter("timezone");
        if (timezoneParam != null && !timezoneParam.isEmpty()) {
            try {
                ZoneId zone = ZoneId.of(timezoneParam);
                Cookie cookie = new Cookie(COOKIE_NAME, timezoneParam);
                response.addCookie(cookie);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid timezone parameter");
                return;
            }
        } else {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(COOKIE_NAME)) {
                    timezoneParam = cookie.getValue();
                    break;
                }
            }
            if (timezoneParam == null) {
                timezoneParam = "GMT";
            }
        }
        TimeZone timezone = TimeZone.getTimeZone(timezoneParam.replace(' ', '+'));
        Calendar calendar = Calendar.getInstance(timezone);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        dateFormat.setTimeZone(timezone);
        String formattedDate = dateFormat.format(calendar.getTime());
        request.setAttribute("time", formattedDate);
        Context simpleContext = new Context();
        simpleContext.setVariable("time", formattedDate);
        engine.process("time", simpleContext, response.getWriter());
        response.getWriter().close();
    }

    @Override
    public void init() throws ServletException {

        engine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("./templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);

    }
}
