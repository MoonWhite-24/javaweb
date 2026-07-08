package com.market.api.controller.admin;

import com.market.common.model.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminUploadController {

    private static final Logger log = LoggerFactory.getLogger(AdminUploadController.class);

    private static final String UPLOAD_DIR = "/opt/javaweb2.0/static/images/products/";
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    ));
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    // All cluster node IPs for cross-node sync
    private static final String[] CLUSTER_NODES = {"192.168.200.100", "192.168.200.101", "192.168.200.102"};

    @PostMapping("/upload/image")
    public R<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            return R.error("请选择要上传的文件");
        }
        if (file.getSize() > MAX_SIZE) {
            return R.error("文件大小不能超过10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return R.error("仅支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        // Generate unique filename
        String ext = getExtension(file.getOriginalFilename());
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        // Save to local filesystem
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(UPLOAD_DIR + filename);
        try {
            file.transferTo(dest);
            log.info("图片上传成功: {}", filename);
        } catch (IOException e) {
            log.error("图片保存失败", e);
            return R.error("图片保存失败: " + e.getMessage());
        }

        // Sync to other cluster nodes (async, non-blocking)
        syncToOtherNodes(filename);

        // Return the URL path that nginx serves
        String url = "/static/images/products/" + filename;
        return R.ok(url);
    }

    private void syncToOtherNodes(String filename) {
        String localIp = getLocalIp();
        String localPath = UPLOAD_DIR + filename;

        for (String nodeIp : CLUSTER_NODES) {
            if (nodeIp.equals(localIp)) continue;
            String remote = "root@" + nodeIp + ":" + UPLOAD_DIR;
            try {
                // Ensure remote directory exists, then copy file
                ProcessBuilder mkdirPb = new ProcessBuilder("ssh", "-o", "StrictHostKeyChecking=no",
                        "-o", "ConnectTimeout=3", "root@" + nodeIp, "mkdir -p", UPLOAD_DIR);
                mkdirPb.start();

                ProcessBuilder pb = new ProcessBuilder("scp", "-o", "StrictHostKeyChecking=no",
                        "-o", "ConnectTimeout=3", localPath, remote);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
                if (finished && process.exitValue() == 0) {
                    log.info("图片同步到 {} 成功: {}", nodeIp, filename);
                } else {
                    log.warn("图片同步到 {} 失败: {}", nodeIp, filename);
                }
            } catch (Exception e) {
                log.warn("图片同步到 {} 异常: {}", nodeIp, e.getMessage());
            }
        }
    }

    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : ".jpg";
    }
}
