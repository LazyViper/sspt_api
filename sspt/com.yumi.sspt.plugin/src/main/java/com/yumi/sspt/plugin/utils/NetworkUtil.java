package com.yumi.sspt.plugin.utils;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by foresee on 2017/6/30.
 */
public class NetworkUtil {

    /**
     * Logger for this class
     */
    private static Logger logger = LoggerFactory.getLogger(NetworkUtil.class);

    /**
     * 本地IP
     */
    private static String localIp;


    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     *
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？ 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     *
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
     *
     * 用户真实IP为： 192.168.1.110
     *
     * @param request
     * @return
     * @throws IOException
     */
    public final static String getIpAddress(HttpServletRequest request) throws IOException {
        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址

        String ip = request.getHeader("X-Forwarded-For");
        if (logger.isDebugEnabled()) {
            logger.debug("getIpAddress(HttpServletRequest) - X-Forwarded-For - String ip=" + ip);
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
                if (logger.isDebugEnabled()) {
                    logger.debug("getIpAddress(HttpServletRequest) - Proxy-Client-IP - String ip=" + ip);
                }
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
                if (logger.isDebugEnabled()) {
                    logger.debug("getIpAddress(HttpServletRequest) - WL-Proxy-Client-IP - String ip=" + ip);
                }
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
                if (logger.isInfoEnabled()) {
                    logger.debug("getIpAddress(HttpServletRequest) - HTTP_CLIENT_IP - String ip=" + ip);
                }
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
                if (logger.isDebugEnabled()) {
                    logger.debug("getIpAddress(HttpServletRequest) - HTTP_X_FORWARDED_FOR - String ip=" + ip);
                }
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (logger.isDebugEnabled()) {
                    logger.debug("getIpAddress(HttpServletRequest) - getRemoteAddr - String ip=" + ip);
                }
            }
        } else if (ip.length() > 15) {
            String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = (String) ips[index];
                if (!("unknown".equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 获取当前服务的IP地址
     * @return
     */
    public final static String getLocalIp() {

        if (localIp != null) {
            return localIp;
        }

        InetAddress ip = null;
        String strIp = "";
        try {
            Enumeration<?> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            boolean flag = true;
            while (allNetInterfaces.hasMoreElements() && flag) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<?> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) {
                        strIp = ip.toString();
                        strIp = strIp.indexOf("/") != -1 ? strIp.substring(1) : strIp;

                        localIp = strIp;

                        logger.debug("获取到的本机IP是{}", strIp);
                        flag = false;
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("获取本机IP失败", e);
        }
        return strIp;
    }


    private final static String hostname = initHostname();


    public static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static long ipToLong(String ip) {
        InetAddress ipAddr = InetAddresses.forString(ip);
        return ipToLong(ipAddr);
    }

    /**
     * 私有IP范围: 10.0.0.0 - 10.255.255.255, 172.16.0.0 - 172.31.255.255, 192.168.0.0 - 192.168.255.255
     * 判断一个ip是否在某段范围
     *
     * @param current 给定的ip
     * @param from 范围起始地址
     * @param to 范围结束地址
     * @return
     */
    public static boolean range(String current, String from, String to) {
        long fromIp = ipToLong(from);
        long toIp = ipToLong(to);
        long currentIp = ipToLong(current);
        return fromIp <= currentIp && currentIp <= toIp;
    }

    /**
     * 判断所给ip是否是公网ip
     * @param host
     * @return
     */
    public static boolean isPublicIp(String host) {
        String ip = resolve(host);
        return !isPrivateIp(host) && !"127.0.0.1".equals(host);
    }


    /**
     * 根据域名获取到对应的ip
     * @param host  域名或者ip
     * @return
     */
    public static String resolve(String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("解析失败" + host, e);
        }
    }

    /**
     * 判断给定的ip是否是内网ip
     *
     * @param host
     * @return 是就返回true, 反则false
     */
    public static boolean isPrivateIp(String host) {
        String ip = resolve(host);
        try {
            return range(ip, "10.0.0.0", "10.255.255.255") || range(ip, "172.16.0.0", "172.31.255.255")
                    || range(ip, "192.168.0.0", "192.168.255.255");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取内网ip
     *
     * @param includeLoopback 是否包含127.0.0.1
     * @return 内网ip集合
     */
    public static Collection<String> findPrivateHosts(boolean includeLoopback) {
        List<String> ips = Lists.newArrayListWithExpectedSize(4);
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
                while (addresses.hasMoreElements()) {
                    String address = addresses.nextElement().getHostAddress();
                    if (isPrivateIp(address)) {
                        ips.add(address);
                    } else if ("127.0.0.1".equals(address) && includeLoopback) {
                        ips.add(address);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取内网ip出错", e);
            throw Throwables.propagate(e);
        }
        return ips;
    }

    public static String hostname() {
        return hostname;
    }

    private static String initHostname() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "UNKNOWN";
            logger.warn("获取本地机器名失败", e);
        }
        return hostname;
    }

    public static void main(String[] args) throws UnknownHostException {
        String ip = InetAddress.getByName("baidu.com").getHostAddress();
        System.out.println(ip);
        ip = InetAddress.getByName("111.13.101.208").getHostAddress();
        System.out.println(ip);
        ip = InetAddress.getByName("192.168.0.18111").getHostAddress();
        System.out.println(ip);
    }
}
