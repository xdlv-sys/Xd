package xd.fw;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FwUtil {

    public static String UTF8 = "UTF-8";
    //static int[] months = new int[]{0,31,29,31,30,31,30,31,31,30,31,30,31};
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getValidateCode() {
        return String.format("%04d", Math.abs(new Random().nextInt(9999)));
    }

    public static Object getBean(String name) {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        return wac == null ? null : wac.getBean(name);
    }

    // sdf is not thread safe so we need add synchronized
    public synchronized static int getLastDayInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(String.format("%d-%02d-%02d 00:00:00", year, month, 1)));
        } catch (Exception e) {
            throw new IllegalArgumentException("can not parse data", e);
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int[] getLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1};
    }

    public static String md5(String plainText) throws Exception {
        byte[] secretBytes = MessageDigest.getInstance("md5").digest(
                plainText.getBytes(HttpClientTpl.UTF8));
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static void invokeBeanFields(Object o, BeanFieldProcess p) throws IllegalAccessException {
        Field[] fields = o.getClass().getDeclaredFields();
        Object v;
        for (Field f : fields) {
            f.setAccessible(true);
            v = f.get(o);
            p.process(f, v);
        }
    }

    public static void invokeBeanFieldsWidthConditions(Object o, BeanFieldProcess p, BeanFieldCondition condition) throws IllegalAccessException {
        invokeBeanFields(o, (f, v) -> {
            if (condition.accept(f, v)) {
                p.process(f, v);
            }
        });
    }

    public interface BeanFieldCondition {
        boolean accept(Field f, Object o);
    }

    public interface BeanFieldProcess {
        void process(Field f, Object o);
    }

    public interface SafeEachProcess<T> {
        void process(T t);
    }

    public static <T> void safeEach(Collection<T> list, SafeEachProcess<T> p) {
        if (list == null || list.size() < 1) {
            return;
        }
        list.forEach(p::process);
    }

    public static boolean verify(Map<String, Object> params, String key) {
        List<String> list = new ArrayList<String>();
        String sign = null;
        String value;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof String[]) {
                value = ((String[]) entry.getValue())[0];
            } else {
                value = String.valueOf(entry.getValue());
            }
            if (entry.getKey().equals("sign")) {
                sign = value;
                continue;
            }
            if (StringUtils.isBlank(value)) {
                continue;
            }
            list.add(entry.getKey() + "=" + value + "&");
        }
        return getSign(list, key).equals(sign);
    }

    public static String getSign(List<String> params, String key) {
        int size = params.size();
        String[] arrayToSort = params.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(arrayToSort[i]);
        }
        sb.append("key=").append(key);
        return MD5.MD5Encode(sb.toString());
    }

    public static String executeCmd(AsyncTaskExecutor taskExecutor, File directory, String[] cmd) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(directory);
        Process process = null;
        BufferedReader br = null;
        BufferedReader br2 = null;
        StringBuilder buffer = new StringBuilder();
        try {
            process = builder.start();
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            br2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            Future<String> info = taskExecutor.submit(new ReaderThread(br));
            Future<String> error = taskExecutor.submit(new ReaderThread(br2));
            buffer.append(info.get(10, TimeUnit.SECONDS)).append(error.get(10, TimeUnit.SECONDS));
        } finally {
            if (process != null) {
                process.waitFor();
            }
            if (br != null) {
                br.close();
            }
            if (br2 != null) {
                br.close();
            }
        }
        return buffer.toString();
    }

    public static void unzip(File zipFile, File destDir) throws IOException {
        try (ZipFile zip = ZipHelper.openZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            ZipEntry zipEntry;
            byte[] buffer = new byte[1024];
            InputStream ins = null;
            int count;
            FileOutputStream os = null;
            while (entries.hasMoreElements()) {
                zipEntry = entries.nextElement();
                File itemFile = new File(destDir, zipEntry.getName());
                try {
                    if (zipEntry.isDirectory()) {
                        if (!itemFile.exists() && !itemFile.mkdirs()){
                            throw new IOException("can not make dir :" + zipEntry.getName());
                        }
                        continue;
                    }
                    if (itemFile.exists() && !itemFile.delete()){
                        throw new IOException("can no delete file:" + itemFile);
                    }
                    os = new FileOutputStream(itemFile);
                    ins = zip.getInputStream(zipEntry);
                    while ((count = ins.read(buffer)) > -1) {
                        os.write(buffer, 0, count);
                    }
                } finally {
                    if (os != null) os.close();
                    if (ins != null) ins.close();
                }
            }
        }
    }

    static class ReaderThread implements Callable<String> {
        static Logger logger = LoggerFactory.getLogger(ReaderThread.class);
        BufferedReader br;
        ReaderThread(BufferedReader br) {
            this.br = br;
        }

        public String call() {
            StringBuilder lines = new StringBuilder();
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    lines.append(line).append("\n");
                    logger.info(line);
                }
            } catch (IOException e) {
                logger.error("", e);
            }
            return lines.toString();
        }
    }

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    public static byte[] qrCode(String content, int width, int height) throws Exception {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN,0);
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        try(ByteArrayOutputStream os = new ByteArrayOutputStream()){
            ImageIO.write(image,"png", os);
            return os.toByteArray();
        }
    }

    final static SimpleDateFormat orderSdf = new SimpleDateFormat("HHmmssyyyyMMddSSS");
    public static synchronized String createOutTradeNo(){
        return String.valueOf(orderSdf.format(new Date()));
    }



    public static void main(String[] args) throws Exception {
        unzip(new File("C:\\Work\\Java\\output\\war.zip"), new File("C:\\Work\\Java\\output\\war"));
    }
}
