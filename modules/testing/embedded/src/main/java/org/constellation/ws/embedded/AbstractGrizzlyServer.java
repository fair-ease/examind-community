/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.ws.embedded;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.StorageConnector;
import org.apache.sis.util.collection.BackingStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.util.Util;
import org.geotoolkit.image.io.XImageIO;

import javax.imageio.ImageReader;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.IOUtils;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IMetadataBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.ISensorBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.business.IDataCoverageJob;
import org.constellation.configuration.AppProperty;
import org.constellation.configuration.Application;
import org.geotoolkit.nio.IOUtilities;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.catalina.Context;
import org.apache.sis.test.xml.DocumentComparator;
import org.constellation.business.IPyramidBusiness;
import org.constellation.business.IUserBusiness;
import org.constellation.util.NodeUtilities;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.w3c.dom.Node;

/**
 * Launches a SPring boot server in a thread at the beginning of the testing process
 * and kill it when it is done.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
@Configuration
@ImportResource({"classpath:/cstl/spring/test-context-complete.xml"})
public abstract class AbstractGrizzlyServer {

    public static String[] CSTL_SPRING_PACKAGE = new String[] {
            "org.constellation.configuration.ws",
            "org.constellation.map.ws.rs",
            "org.constellation.metadata.ws.rs",
            "org.constellation.ws.rs.provider",
            "org.constellation.coverage.ws.rs",
            "org.constellation.wfs.ws.rs",
            "org.constellation.sos.ws.rs",
            "org.constellation.sos.ws.rs.provider",
            "org.constellation.wmts.ws.rs",
            "org.constellation.metadata.ws.rs.provider",
            "org.constellation.wps.ws.rs",
            "com.examind.sts.ws.rs",
            "org.constellation.thesaurus.ws.rs"};

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.ws.embedded");

    private static ConfigurableApplicationContext ctx;

    protected static MarshallerPool pool;

    protected IServiceBusiness serviceBusiness;
    protected ILayerBusiness layerBusiness;
    protected IProviderBusiness providerBusiness;
    protected IDatasetBusiness datasetBusiness;
    protected IDataBusiness dataBusiness;
    protected IMetadataBusiness metadataBusiness;
    protected ISensorBusiness sensorBusiness;
    protected IUserBusiness userBusiness;
    protected IPyramidBusiness pyramidBusiness;
    protected IDataCoverageJob dataCoverageJob;

    protected static Class controllerConfiguration;

    /**
     * Initialize and start the server.
     */
    public void startServer() throws Exception {
        // Hack: force french locale to comply with hard-coded test identifiers (see WMSRequestsTest#testWMSGetFeatureInfoPlainCoveragePng
        Locale.setDefault(Locale.FRANCE);
        ctx = SpringApplication.run(AbstractGrizzlyServer.class, new String[0]);

        serviceBusiness = SpringHelper.getBean(IServiceBusiness.class);
        layerBusiness = SpringHelper.getBean(ILayerBusiness.class);
        providerBusiness = SpringHelper.getBean(IProviderBusiness.class);
        datasetBusiness = SpringHelper.getBean(IDatasetBusiness.class);
        dataBusiness = SpringHelper.getBean(IDataBusiness.class);
        metadataBusiness = SpringHelper.getBean(IMetadataBusiness.class);
        sensorBusiness = SpringHelper.getBean(ISensorBusiness.class);
        userBusiness = SpringHelper.getBean(IUserBusiness.class);
        pyramidBusiness = SpringHelper.getBean(IPyramidBusiness.class);
        dataCoverageJob = SpringHelper.getBean(IDataCoverageJob.class);
    }

    /**
     * Stop the grizzly server, if it is still alive.
     */
    public static void stopServer() {
        if (ctx != null) {
            SpringApplication.exit(ctx);
            File f = new File("derby.log");
            if (f.exists()) {
                f.delete();
            }
            ctx = null;
        }
    }

    public static int getCurrentPort() {
        return Application.getIntegerProperty(AppProperty.CSTL_PORT, 9090);
    }


    /**
     * needed to start the container
     *
     * @return ServletWebServerFactory
     */
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                super.postProcessContext(context);

                context.addApplicationListener("org.apache.tomcat.websocket.server.WsContextListener");
            }
        };

        tomcat.setPort(getCurrentPort());

        return tomcat;
    }

    /**
     * Manually register a dispatcher servlet with the selected controllers
     * @return
     */
    @Bean
    public ServletRegistrationBean examindapi() {
        final DispatcherServlet servlet = new DispatcherServlet();
        final AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.scan("org.constellation.api.rest");
        appContext.register(RestAPIControllerConfig.class);
        servlet.setApplicationContext(appContext);
        final ServletRegistrationBean servletBean = new ServletRegistrationBean(servlet, "/API/*");
        servletBean.setName("examindapi");
        servletBean.setLoadOnStartup(1);
        servletBean.setAsyncSupported(true);
        return servletBean;
    }

    @Bean
    public ServletRegistrationBean hosted() {
        final DispatcherServlet servlet = new DispatcherServlet();
        final AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.scan("org.constellation.ws.embedded.hosted");
        appContext.register(RestAPIControllerConfig.class);
        servlet.setApplicationContext(appContext);
        final ServletRegistrationBean servletBean = new ServletRegistrationBean(servlet, "/hosted/*");
        servletBean.setName("hosted");
        servletBean.setLoadOnStartup(1);
        servletBean.setAsyncSupported(true);
        return servletBean;
    }


    /**
     * Manually register a dispatcher servlet with the selected controllers
     * @return
     */
    @Bean
    public ServletRegistrationBean ogcServiceServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.scan(CSTL_SPRING_PACKAGE);
        if (controllerConfiguration != null) {
            applicationContext.register(controllerConfiguration);
        }
        dispatcherServlet.setApplicationContext(applicationContext);
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/WS/*");
        servletRegistrationBean.setName("ogc-WS");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }

    public void waitForRestStart(final String servicetype, String serviceName) throws Exception {
        waitForRestStart("http://localhost:"+ getCurrentPort() +"/WS/"+servicetype.toLowerCase()+"/"+serviceName);
    }

    public void waitForRestStart(final String url) throws Exception {
        LOGGER.log(Level.INFO, "Waiting for service to start at {0}", url);
        final URL u = new URL(url);
        int resCode = 404;
        int cpt = 0;
        while (resCode == 404) {
            Thread.sleep(1 * 2000);
            HttpURLConnection conec = (HttpURLConnection) u.openConnection();
            try {
                conec.connect();
                resCode = conec.getResponseCode();
            } catch (IOException e) {}
            if (cpt == 100) {
                throw new Exception("The rest service at "+url+" never start");
            }
            cpt++;
        }
    }

    protected static String getStringResponse(final URLConnection conec) throws UnsupportedEncodingException, IOException {
        return getStringResponse(conec, 200);
    }
    
    protected static String getStringResponse(final URLConnection conec, int expectedHttpCode) throws UnsupportedEncodingException, IOException {
        InputStream is;
        if (((HttpURLConnection)conec).getResponseCode() == expectedHttpCode) {
            is = conec.getInputStream();
        } else {
            is = ((HttpURLConnection)conec).getErrorStream();
        }
        final StringWriter sw     = new StringWriter();
        final BufferedReader in   = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        char [] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        String xmlResult = sw.toString();
        return xmlResult;
    }

    protected static String getStringResponse(final URL url) throws UnsupportedEncodingException, IOException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return getStringResponse(conn);
    }

    protected static String putStringResponse(final URL url) throws UnsupportedEncodingException, IOException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        return getStringResponse(conn);
    }

    protected static String getStringFromFile(String filePath) throws UnsupportedEncodingException, IOException {
        String xmlExpResult = IOUtilities.toString(Util.getResourceAsStream(filePath), Charset.forName("UTF-8"));
        //we unformat the expected result
        //xmlExpResult = xmlExpResult.replace("\n", "");

        return xmlExpResult;
    }

    protected static void postRequestFile(URLConnection conec, String filePath, String contentType) throws IOException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", contentType);
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final InputStream is = Util.getResourceAsStream(filePath);
        final StringWriter sw = new StringWriter();
        final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        char[] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        wr.write(sw.toString());
        wr.flush();
        in.close();
    }

    protected static void putRequestFile(URLConnection conec, String filePath, String contentType) throws IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", contentType);
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final InputStream is = Util.getResourceAsStream(filePath);
        final StringWriter sw = new StringWriter();
        final BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        char[] buffer = new char[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            sw.append(new String(buffer, 0, size));
        }
        wr.write(sw.toString());
        wr.flush();
        in.close();
    }

    protected static void postRequestFile(URLConnection conec, String filePath) throws IOException {
        postRequestFile(conec, filePath, "text/xml");
    }

    protected static void putRequestFile(URLConnection conec, String filePath) throws IOException {
        putRequestFile(conec, filePath, "text/xml");
    }

    protected static void postRequestPlain(URLConnection conec, String request) throws IOException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "text/plain");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        wr.write(request);
        wr.flush();
    }

    /**
     * Returned the {@link BufferedImage} from an URL requesting an image.
     *
     * @param url  The url of a request of an image.
     * @param mime The mime type of the image to return.
     *
     * @return The {@link BufferedImage} or {@code null} if an error occurs.
     * @throws IOException
     */
    protected static BufferedImage getImageFromURL(final URL url, final String mime) throws IOException {
        /* Try to bypass image reader instabilities by loading source content in memory first.
         * Note that the code related to the storage connector still properly close streams, even if it is not
         * necessary, in case of future changes.
         * TODO: upon Java 9 migration, use InputStream.readFully() instead.
         */

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (final InputStream stream = getInputOrErrorStream(conn)) {
            int read;
            int safeguard= 1000;
            byte[] buf = new byte[4096];
            do {
                read = stream.read(buf);
                if (read > 0) output.write(buf, 0, read);
            } while (read >= 0 && --safeguard >= 0);
            if (safeguard < 0) throw new IOException("Cannot fully read image in acceptable time.");
        }

        if (conn.getResponseCode() >=400) {
            throw new RuntimeException(conn.getResponseMessage());
        }

        // Try to get the image from the url.
        final ImageInputStream in = fromImageIO(output.toByteArray());
        try {
            //in = new StorageConnector(output.toByteArray()).getStorageAs(ImageInputStream.class);
            //try (AutoCloseable c = in::close) {
                final ImageReader reader = XImageIO.getReaderByMIMEType(mime, in, true, true);
                try {
                    return reader.read(0);
                } finally {
                    XImageIO.close(reader);
                }
            //}
        } catch (IOException io) {
            throw io;
        } catch (Exception e) {
            throw new IOException(e);
        }
        // For debugging, uncomment the JFrame creation and the Thread.sleep further,
        // in order to see the image in a popup.
//        javax.swing.JFrame frame = new javax.swing.JFrame();
//        frame.setContentPane(new javax.swing.JLabel(new javax.swing.ImageIcon(image)));
//        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        try {
//            Thread.sleep(5 * 1000);
//            frame.dispose();
//        } catch (InterruptedException ex) {
//            assumeNoException(ex);
//        }
    }

    private static ImageInputStream fromStorageConnector(final byte[] content) {
        try {
            return new StorageConnector(content).getStorageAs(ImageInputStream.class);
        } catch (DataStoreException e) {
            throw new BackingStoreException(e);
        }
    }

    private static ImageInputStream fromImageIO(final byte[] content) {
        final InputStream stream = new ByteArrayInputStream(content);
        return new MemoryCacheImageInputStream(stream);
    }
    
    /**
     * Returned the {@link BufferedImage} from an URL requesting an image.
     *
     * @param url  The url of a request of an image.
     * @param format The format of the image to return.
     *
     * @return The {@link BufferedImage} or {@code null} if an error occurs.
     * @throws IOException
     */
    protected static BufferedImage getImageFromURLByFormat(final URL url, final String format) throws IOException {
        // Try to get the image from the url.
        final InputStream in = url.openStream();
        final ImageInputStream win = ImageIO.createImageInputStream(in);
        final ImageReader reader = XImageIO.getReaderByFormatName(format, win, true, true);
        final BufferedImage image = reader.read(0);
        XImageIO.close(reader);
        reader.dispose();
        // For debugging, uncomment the JFrame creation and the Thread.sleep further,
        // in order to see the image in a popup.
//        javax.swing.JFrame frame = new javax.swing.JFrame();
//        frame.setContentPane(new javax.swing.JLabel(new javax.swing.ImageIcon(image)));
//        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        try {
//            Thread.sleep(5 * 1000);
//            frame.dispose();
//        } catch (InterruptedException ex) {
//            assumeNoException(ex);
//        }
        return image;
    }

    protected static BufferedImage getImageFromPostKvp(final URL url, final Map<String, String> parameters, final String mime) throws IOException {
        final URLConnection conec = url.openConnection();
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : parameters.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.deleteCharAt(sb.length() -1);
        wr.write(sb.toString());
        wr.flush();

        // Try to get the image from the url.
        final InputStream in = conec.getInputStream();
        final ImageReader reader = XImageIO.getReaderByMIMEType(mime, in, true, true);
        final BufferedImage image = reader.read(0);
        XImageIO.close(reader);
        reader.dispose();
        // For debugging, uncomment the JFrame creation and the Thread.sleep further,
        // in order to see the image in a popup.
//        javax.swing.JFrame frame = new javax.swing.JFrame();
//        frame.setContentPane(new javax.swing.JLabel(new javax.swing.ImageIcon(image)));
//        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        try {
//            Thread.sleep(5 * 1000);
//            frame.dispose();
//        } catch (InterruptedException ex) {
//            assumeNoException(ex);
//        }
        return image;
    }

    protected static void postRequestObject(URLConnection conec, Object request) throws IOException, JAXBException {
        postRequestObject(conec, request, pool);
    }

    protected static void postRequestObject(URLConnection conec, Object request, MarshallerPool pool) throws IOException, JAXBException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/xml");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        if (request != null) {
            final StringWriter sw = new StringWriter();
            Marshaller marshaller = pool.acquireMarshaller();
            marshaller.marshal(request, sw);
            pool.recycle(marshaller);
            wr.write(sw.toString());
        }
        wr.flush();
    }

    protected static void postJsonRequestObject(URLConnection conec, Object request) throws IOException, JAXBException {
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/json");
        conec.setRequestProperty("Accept",       "application/json");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        if (request != null) {
            final ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(request);
            wr.write(json);
        }
        wr.flush();
    }

    protected static void putRequestObject(URLConnection conec, Object request, MarshallerPool pool) throws IOException, JAXBException {
        putRequestObject(conec, request, pool, "application/xml", null);
    }

    protected static void putRequestObject(URLConnection conec, Object request, MarshallerPool pool, String mimeType, String acceptMimeType) throws IOException, JAXBException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", mimeType);
        if (acceptMimeType != null) {
            conec.setRequestProperty("Accept", acceptMimeType);
        }
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        final StringWriter sw = new StringWriter();
        Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(request, sw);
        pool.recycle(marshaller);
        wr.write(sw.toString());
        wr.flush();
    }

    protected static void putJsonRequestObject(URLConnection conec, Object request) throws IOException, JAXBException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        conec.setDoOutput(true);
        conec.setRequestProperty("Content-Type", "application/json");
        final OutputStreamWriter wr = new OutputStreamWriter(conec.getOutputStream());
        if (request != null) {
            final ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(request);
            wr.write(json);
        }
        wr.flush();
    }

    protected static Object unmarshallResponsePut(final URLConnection conec) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        return unmarshallStream(conec.getInputStream());
    }

     protected static Object unmarshallJsonResponsePut(final URLConnection conec, Class type) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("PUT");
        return unmarshallJsonStream(conec.getInputStream(), type);
    }

    protected static Object unmarshallResponsePost(final URLConnection conec) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("POST");
        return unmarshallStream(conec.getInputStream());
    }

    protected static Object unmarshallJsonResponsePost(final URLConnection conec, Class type) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("POST");
        return unmarshallJsonStream(conec.getInputStream(), type);
    }

    protected static Object unmarshallResponseDelete(final URLConnection conec) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("DELETE");
        return unmarshallStream(conec.getInputStream());
    }

    protected static Object unmarshallJsonResponseDelete(final URLConnection conec, Class type) throws JAXBException, IOException {
        HttpURLConnection httpCon = (HttpURLConnection) conec;
        httpCon.setRequestMethod("DELETE");
        return unmarshallJsonStream(conec.getInputStream(), type);
    }

    protected static Object unmarshallJsonResponse(final URLConnection conec, Class type) throws JAXBException, IOException {
        return unmarshallJsonStream(conec.getInputStream(), type);
    }

    protected static Object unmarshallResponse(final URLConnection conec, boolean print) throws JAXBException, IOException {
        return unmarshallStream(conec.getInputStream(), print);
    }

    protected static Object unmarshallResponse(final URLConnection conec) throws JAXBException, IOException {
        return unmarshallStream(conec.getInputStream());
    }

    protected static Object unmarshallResponse(final URL url) throws JAXBException, IOException {
        return unmarshallResponse(url, false);
    }

    protected static Object unmarshallResponse(final URL url, boolean print) throws JAXBException, IOException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (InputStream is = getInputOrErrorStream(conn)) {
            return unmarshallStream(is, print);
        }
    }

    private static InputStream getInputOrErrorStream(HttpURLConnection conn) throws IOException {
        InputStream is;
        // don't follow redirection, anything above is error
        final int code = conn.getResponseCode();
        if (code >= 400) {
            is = conn.getErrorStream();
            if (is == null) throw new RuntimeException(String.format(
                    "HTTP code: %d%nResponse message: %s%nDetails: %s%n",
                    code, conn.getResponseMessage(), conn.toString()
            ));
        } else if (code >=300) {
            throw new RuntimeException("Redirection not supported");
        } else {
            is = conn.getInputStream();
        }
        return is;
    }

    protected static Object unmarshallJsonResponse(final URL url, Class type) throws JAXBException, IOException {
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try (InputStream is = getInputOrErrorStream(conn)) {
            return unmarshallJsonStream(is, type);
        }
    }

    private static Object unmarshallStream(final InputStream is) throws IOException, JAXBException {
        return unmarshallStream(is, false);
    }

    private static Object unmarshallStream(final InputStream is, boolean print) throws IOException, JAXBException {

        //JDK-8 : ensure xml reader are allowed to access external DTD files when needed
        System.setProperty("javax.xml.accessExternalSchema", "all");
        System.setProperty("javax.xml.accessExternalDTD", "all");

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");

        Object obj;
        try {
            if (print) {
                LOGGER.info(writer.toString());
            }
            obj = unmarshaller.unmarshal(new StringReader(writer.toString()));
            pool.recycle(unmarshaller);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "JAXB Error received while trying to read:{0}", writer.toString());
            throw ex;
        }

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        return obj;
    }

    private static Object unmarshallJsonStream(final InputStream is, Class type) throws IOException, JAXBException {

        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");

        Object obj;
        try {
            obj = mapper.readValue(writer.toString(),type);
        } catch (JsonParseException ex) {
            LOGGER.log(Level.WARNING, "JSON Error received while trying to read:{0}", writer.toString());
            throw ex;
        }

        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement) obj).getValue();
        }
        return obj;
    }

    protected static String removeUpdateSequence(final String xml) {
        String s = xml;
        s = s.replaceAll("updateSequence=\"[^\"]*\" ", "");
        return s;
    }

    protected static void domCompare(final Object actual, final Object expected, List<String> extraIgnoredAttributes) throws Exception {
        domCompare(actual, expected, extraIgnoredAttributes, new ArrayList<>());
    }

    protected static void domCompare(final Object actual, final Object expected, List<String> extraIgnoredAttributes, List<String> extraIgnoredNodes) throws Exception {

        final DocumentComparator comparator = new DocumentComparator(expected, actual);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.ignoredAttributes.add("updateSequence");
        comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
        if (extraIgnoredAttributes != null) {
            comparator.ignoredAttributes.addAll(extraIgnoredAttributes);
        }
        if (extraIgnoredNodes != null) {
            comparator.ignoredNodes.addAll(extraIgnoredNodes);
        }
        comparator.compare();
    }

    protected static void domCompare(final Object actual, final Object expected) throws Exception {
        domCompare(actual, expected, new ArrayList<>());
    }

    /**
     * used for debug
     *
     * @param n
     * @return
     * @throws Exception
     */
    protected static String getStringFromNode(final Node n) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        NodeUtilities.secureFactory(tf);
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(n), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }

    /**
     * I don't know why but the bean seems to be emptyed between 2 tests
     * @return
     */
    protected IProviderBusiness getProviderBusiness() {
        if (providerBusiness == null) {
            providerBusiness = SpringHelper.getBean(IProviderBusiness.class);
        }
        return providerBusiness;
    }

    /**
     * I don't know why but the bean seems to be emptyed between 2 tests
     * @return
     */
    protected IDataBusiness getDataBusiness() {
        if (dataBusiness == null) {
            dataBusiness = SpringHelper.getBean(IDataBusiness.class);
        }
        return dataBusiness;
    }

    /**
     * I don't know why but the bean seems to be emptyed between 2 tests
     * @return
     */
    protected ISensorBusiness getSensorBusiness() {
        if (sensorBusiness == null) {
            sensorBusiness = SpringHelper.getBean(ISensorBusiness.class);
        }
        return sensorBusiness;
    }

}
