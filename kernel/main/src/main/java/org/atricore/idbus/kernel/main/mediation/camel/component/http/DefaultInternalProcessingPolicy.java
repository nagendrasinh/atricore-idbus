package org.atricore.idbus.kernel.main.mediation.camel.component.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atricore.idbus.kernel.main.util.ConfigurationContext;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author <a href=mailto:sgonzalez@atricore.org>Sebastian Gonzalez Oyuela</a>
 */
public class DefaultInternalProcessingPolicy implements InternalProcessingPolicy, InitializingBean {

    private static final Log logger = LogFactory.getLog(DefaultInternalProcessingPolicy .class);

    private ConfigurationContext kernelConfig;

    private List<String> excludedUrls = new ArrayList<String>();

    private List<String> includedUrls = new ArrayList<String>();

    public void afterPropertiesSet() throws Exception {

        String excludedUrlsCsv = kernelConfig.getProperty("binding.http.followRedirects.excludeUrls");
        if (excludedUrlsCsv != null) {
            StringTokenizer st = new StringTokenizer(excludedUrlsCsv, ",");
            while (st.hasMoreTokens()) {
                String url = st.nextToken().trim();
                logger.info("Not following redirects for ["+url+"]");
                excludedUrls.add(url);
            }
        }

        String includedUrlsCsv = kernelConfig.getProperty("binding.http.followRedirects.includeUrls");
        if (includedUrlsCsv != null) {
            StringTokenizer st = new StringTokenizer(includedUrlsCsv, ",");
            while (st.hasMoreTokens()) {
                String url = st.nextToken().trim();
                logger.info("Not following redirects for ["+url+"]");
                includedUrls.add(url);
            }
        }

    }


    public boolean match(HttpServletRequest originalReq, String redirectUrl) {

        // Force includes/excludes
        for (String includedUrl : includedUrls) {
            if (redirectUrl.startsWith(includedUrl)) {
                if (logger.isTraceEnabled())
                    logger.trace("Following, Matching URL to [" + includedUrl + "]");
                return true;
            }
        }

        for (String excludedUrl : excludedUrls) {
            if (redirectUrl.startsWith(excludedUrl)) {
                if (logger.isTraceEnabled())
                    logger.trace("Not Following, Matching URL to ["+excludedUrl+"]");

                return false;
            }
        }

        // See if we're redirected to the same host we started processing
        StringBuffer originalUrl = originalReq.getRequestURL();
        String ctxPath = originalReq.getContextPath();
        if (ctxPath.equals("")) {
            // TODO : Root context needs special treatment.
            throw new RuntimeException("Cannot work with mediation on root context !");
        }

        int ctxEnd = originalUrl.indexOf(ctxPath) + ctxPath.length();
        return redirectUrl.startsWith(originalUrl.substring(0, ctxEnd));


    }

    public boolean match(HttpServletRequest req) {

        // Already internal, ignore it.
        if (req.getHeader("IDBUS-PROXIED-REQUEST") != null)
            return false;

        // Do not proxy POST methods
        if (!req.getMethod().equals("GET"))
            return false;

        // Do not proxy SOAP requests
        if (req.getPathInfo().contains("/SOAP"))
            return false;

        StringBuffer reqUrl = req.getRequestURL();
        String requestUrl = reqUrl.toString();

        // Force includes/excludes
        for (String includedUrl : includedUrls) {
            if (requestUrl.startsWith(includedUrl)) {
                if (logger.isTraceEnabled())
                    logger.trace("Following, Matching URL to ["+includedUrl+"]");
                return true;
            }
        }

        for (String excludedUrl : excludedUrls) {
            if (requestUrl.startsWith(excludedUrl)) {
                if (logger.isTraceEnabled())
                    logger.trace("Not Following, Matching URL to ["+excludedUrl+"]");

                return false;
            }
        }


        return true;

    }

    public List<String> getExcludedUrls() {
        return excludedUrls;
    }

    public void setExcludedUrls(List<String> excludedUrls) {
        this.excludedUrls = excludedUrls;
    }

    public List<String> getIncludedUrls() {
        return includedUrls;
    }

    public void setIncludedUrls(List<String> includedUrls) {
        this.includedUrls = includedUrls;
    }

    public ConfigurationContext getKernelConfig() {
        return kernelConfig;
    }

    public void setKernelConfig(ConfigurationContext kernelConfig) {
        this.kernelConfig = kernelConfig;
    }
}
