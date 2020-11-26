package com.rbkmoney.orgmanager.servlet;

import com.rbkmoney.orgmanagement.AuthContextProviderSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/auth-context")
@Slf4j
@RequiredArgsConstructor
public class AuthContextProviderServlet extends GenericServlet {

    private Servlet thriftServlet;

    private final AuthContextProviderSrv.Iface authContextProvider;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(AuthContextProviderSrv.Iface.class, authContextProvider);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
