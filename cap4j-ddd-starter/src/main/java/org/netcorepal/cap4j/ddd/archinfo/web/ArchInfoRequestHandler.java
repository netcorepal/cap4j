package org.netcorepal.cap4j.ddd.archinfo.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import org.netcorepal.cap4j.ddd.archinfo.ArchInfoManager;
import org.netcorepal.cap4j.ddd.archinfo.model.ArchInfo;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 架构信息
 *
 * @author binking338
 * @date 2024/11/24
 */
@RequiredArgsConstructor
public class ArchInfoRequestHandler implements HttpRequestHandler {
    private final ArchInfoManager archInfoManager;
    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ArchInfo archInfo = archInfoManager.getArchInfo();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json; charset=utf-8");
        response.getWriter().println(JSON.toJSONString(archInfo, SerializerFeature.SortField));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
