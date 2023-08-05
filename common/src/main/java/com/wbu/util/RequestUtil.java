package com.wbu.util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @auther 11852
 * @create 2023/8/1
 */
public class RequestUtil {
    /**
     * 跟踪原有的客户端IP地址和原来客户端请求的服务器地址
     * 存在多级代理的可能性,多级代理取X-Forwarded-For中第一个非unknown的有效IP字符串
     *
     * request.getRemoteAddr() ：获得客户端的ip地址
     *
     * request.getRemoteHost()：获得客户端的主机名
     */
    private static final String[] HEADER_CONFIG = {
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
    };
    private static String serverIp;

    static {
        InetAddress ia = null;
        try {
            serverIp = ia.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取客户端真实ip
     * @param request
     * @return ip
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = null;
        for (String config : HEADER_CONFIG) {
            ip = request.getHeader(config);
            if (ip ==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){
                break;
            }
        }
        /**
         * 如果ip==null 则这说明没有被代理
         */
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        //当请求地址写localhost的话， request.getLocalAddr() 获取到的地址是0:0:0:0:0:0:0:1
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = serverIp;
        }
        return ip;
    }

    /**
     * linux 环境下使用
     * @return
     */
    public static InetAddress getLocalHostExactAddress() {
        try {
            InetAddress candidateAddress = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            return inetAddr;
                        }

                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }

                    }
                }
            }

            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            throw new RuntimeException("获取本机 ip 失败");
        }
//        //获取所有网络接口
//        try {
//            InetAddress candidateAddress = null;
//            Enumeration<NetworkInterface> networkInterfaces = null;
//            networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while (networkInterfaces.hasMoreElements()){
//                //获取网络接口
//                NetworkInterface networkInterface = networkInterfaces.nextElement();
//                //获取网络接口中的所有ip
//                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
//                while (inetAddresses.hasMoreElements()){
//                    InetAddress inetAddress = inetAddresses.nextElement();
//                    //排除回环地址类型
//                    if (!inetAddress.isLoopbackAddress()){
//                        //这才是我们想要的
//                        if (inetAddress.isSiteLocalAddress()){
//                            return inetAddress;
//                        }
//                        if (candidateAddress == null){
//                            candidateAddress = inetAddress;
//                        }
//                    }
//                }
//            }
//            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
//        } catch (Exception e) {
//            throw new RuntimeException("获取本机ip失败");
//        }
    }

    public static String getLocalHost() {
        return getLocalHostExactAddress().getHostAddress();
    }

}
