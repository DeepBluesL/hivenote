package com.hivenote.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hivenote.common.Result;
import com.hivenote.entity.Notification;
import com.hivenote.entity.User;
import com.hivenote.service.INotificationService;
import com.hivenote.service.IUserService;
import com.hivenote.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private INotificationService notificationService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${google.frontend-url}")
    private String frontendUrl;

    public static Map<String, User> sessionMap = new HashMap<>();

    private Map<String, Object> buildUserInfoMap(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("email", user.getEmail());
        info.put("avatar", user.getAvatar());
        info.put("status", user.getStatus());
        info.put("type", user.getType() != null ? user.getType() : 0);
        info.put("school", user.getSchool());
        info.put("showEmail", user.getShowEmail() != null ? user.getShowEmail() : 1);
        info.put("showStudentId", user.getShowStudentId() != null ? user.getShowStudentId() : 1);
        info.put("avatarBanned", user.getAvatarBanned() != null ? user.getAvatarBanned() : 0);
        info.put("createTime", user.getCreateTime());
        return info;
    }

    /**
     * 1. 登录
     * 前端: request({ url: '/user/login', method: 'post', data: data })
     * 注意：前端传的是 JSON Body，所以要用 @RequestBody
     */
    @PostMapping("/user/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        // 参数校验
        if (loginUser.getUsername() == null || loginUser.getUsername().trim().isEmpty()) {
            return Result.error("学号不能为空");
        }
        if (loginUser.getPassword() == null || loginUser.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 调用 Service 查询用户
        User user = userService.getOne(new QueryWrapper<User>()
                .eq("username", loginUser.getUsername()));

        if (user == null) {
            return Result.error("账号不存在");
        }

        // 验证密码（数据库中的密码是加密的，需要对输入的密码加密后比对）
        if (!PasswordUtil.verify(loginUser.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        // 生成 Token (使用 Hutool 工具)
        String token = IdUtil.simpleUUID();
        sessionMap.put(token, user); // 存入会话

        // 构造返回数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", token);
        resultMap.put("userInfo", buildUserInfoMap(user));

        return Result.success(resultMap);
    }

    /**
     * 2. 注册
     * 前端: request({ url: '/user/regist', method: 'post', data: data })
     * 注意：这里前端传的是 JSON Body，所以要用 @RequestBody
     */
    @PostMapping("/user/regist")
    public Result<Object> register(@RequestBody User user) {

        // 参数校验
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("学号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (user.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            return Result.error("昵称不能为空");
        }

        // 检查学号是否已存在
        long count = userService.count(new QueryWrapper<User>().eq("username", user.getUsername()));
        if (count > 0) {
            return Result.error("学号已存在，请更换");
        }

        // 生成用户ID（因为数据库表的id是varchar类型，不是自增）
        user.setId(IdUtil.getSnowflakeNextId());

        // 加密密码
        String encryptedPassword = PasswordUtil.encrypt(user.getPassword());
        user.setPassword(encryptedPassword);

        System.out.println("生成的ID: " + user.getId());
        System.out.println("密码(MD5): " + encryptedPassword);

        // 设置默认头像和初始状态
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        }
        user.setStatus(0); // 0正常
        user.setType(0); // 0普通用户

        // 保存用户
        boolean saved = userService.save(user);

        System.out.println("保存结果: " + (saved ? "成功" : "失败"));
        System.out.println("==================");

        if (saved) {
            return Result.success("注册成功，请登录");
        } else {
            return Result.error("注册失败，请稍后重试");
        }
    }

    /**
     * 3. 获取个人信息
     * 每次从数据库读取最新数据，避免 sessionMap 缓存导致数据不一致
     */
    @GetMapping("/user/info")
    public Result<User> getUserInfo(@RequestHeader("accessToken") String token) {
        User sessionUser = sessionMap.get(token);
        if (sessionUser == null) {
            return Result.error(401, "登录已过期");
        }
        User user = userService.getById(sessionUser.getId());
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 4. 退出登录
     */
    @PostMapping("/user/logout")
    public Result<Object> logout(@RequestHeader(value = "accessToken", required = false) String token) {
        if (token != null && !token.isEmpty()) {
            sessionMap.remove(token);
        }
        return Result.success("退出成功");
    }

    /**
     * 5. 修改密码
     */
    @PostMapping("/user/changePassword")
    public Result<Object> changePassword(@RequestHeader("accessToken") String token,
                                         @RequestBody Map<String, String> params) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "登录已过期");
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return Result.error("原密码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        // 验证原密码（数据库中的密码是加密的）
        User user = userService.getById(currentUser.getId());
        if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 加密新密码并更新
        user.setPassword(PasswordUtil.encrypt(newPassword));
        userService.updateById(user);

        // 更新会话中的用户信息
        sessionMap.put(token, user);

        return Result.success("密码修改成功");
    }

    /**
     * 6. 修改个人资料
     */
    @PostMapping("/user/edit")
    public Result<Map<String, Object>> updateProfile(@RequestHeader("accessToken") String token,
                                        @RequestBody User updateUser) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "登录已过期");
        }

        User user = userService.getById(currentUser.getId());

        if (updateUser.getNickname() != null && !updateUser.getNickname().trim().isEmpty()) {
            user.setNickname(updateUser.getNickname().trim());
        }
        if (updateUser.getEmail() != null) {
            user.setEmail(updateUser.getEmail());
        }
        if (updateUser.getAvatar() != null && !updateUser.getAvatar().trim().isEmpty()) {
            user.setAvatar(updateUser.getAvatar());
        }
        if (updateUser.getSchool() != null) {
            user.setSchool(updateUser.getSchool());
        }
        if (updateUser.getShowEmail() != null) {
            user.setShowEmail(updateUser.getShowEmail());
        }
        if (updateUser.getShowStudentId() != null) {
            user.setShowStudentId(updateUser.getShowStudentId());
        }

        userService.updateById(user);

        // 更新会话中的用户信息
        sessionMap.put(token, user);

        // 返回更新后的用户信息，让前端同步
        Map<String, Object> userInfo = buildUserInfoMap(user);
        return Result.success(userInfo, "资料修改成功");
    }

    /**
     * 管理员获取用户列表
     */
    @GetMapping("/user/admin/list")
    public Result<Object> getAdminUserList(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           String keyword,
                                           Integer status,
                                           @RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权访问");
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.ne("type", 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.and(w -> w.like("username", keyword)
                    .or().like("nickname", keyword)
                    .or().like("email", keyword));
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");

        var result = userService.page(pageParam, qw);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    /**
     * 管理员封禁用户
     */
    @PostMapping("/user/admin/ban/{id}")
    public Result<Object> banUser(@PathVariable Long id,
                                  @RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权操作");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getType() != null && user.getType() == 1) {
            return Result.error("不能封禁管理员");
        }

        user.setStatus(-1);
        userService.updateById(user);

        sessionMap.entrySet().removeIf(entry -> entry.getValue().getId().equals(id));

        return Result.success("用户已封禁");
    }

    /**
     * 管理员解封用户
     */
    @PostMapping("/user/admin/unban/{id}")
    public Result<Object> unbanUser(@PathVariable Long id,
                                    @RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权操作");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStatus(0);
        userService.updateById(user);

        return Result.success("用户已解封");
    }

    /**
     * 7. 管理员登录
     */
    @PostMapping("/user/adminLogin")
    public Result<Map<String, Object>> adminLogin(@RequestBody User loginUser) {
        System.out.println("=== 管理员登录 ===");
        System.out.println("用户名: " + loginUser.getUsername());

        // 参数校验
        if (loginUser.getUsername() == null || loginUser.getUsername().trim().isEmpty()) {
            return Result.error("账号不能为空");
        }
        if (loginUser.getPassword() == null || loginUser.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }

        // 查询用户
        User user = userService.getOne(new QueryWrapper<User>()
                .eq("username", loginUser.getUsername()));

        if (user == null) {
            System.out.println("错误: 账号不存在");
            return Result.error("账号不存在");
        }

        System.out.println("查询到的用户信息:");
        System.out.println("  ID: " + user.getId());
        System.out.println("  用户名: " + user.getUsername());
        System.out.println("  昵称: " + user.getNickname());
        System.out.println("  类型(type): " + user.getType());
        System.out.println("  状态(status): " + user.getStatus());

        // 验证密码
        if (!PasswordUtil.verify(loginUser.getPassword(), user.getPassword())) {
            System.out.println("错误: 密码错误");
            return Result.error("密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != null && user.getStatus() == -1) {
            System.out.println("错误: 账号已被禁用");
            return Result.error("账号已被禁用");
        }

        // 检查是否是管理员 (type = 1)
        System.out.println("开始检查管理员权限...");
        System.out.println("  user.getType() = " + user.getType());
        System.out.println("  user.getType() == null ? " + (user.getType() == null));
        System.out.println("  user.getType() != 1 ? " + (user.getType() != null && user.getType() != 1));

        if (user.getType() == null || user.getType() != 1) {
            System.out.println("错误: 非管理员账号，无权访问");
            System.out.println("  实际type值: " + user.getType());
            return Result.error("非管理员账号，无权访问");
        }

        System.out.println("管理员权限验证通过！");
        System.out.println("用户类型: " + user.getType());

        // 生成 Token
        String token = IdUtil.simpleUUID();
        sessionMap.put(token, user);

        // 构造返回数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", token);
        resultMap.put("userInfo", buildUserInfoMap(user));

        System.out.println("登录成功");
        System.out.println("==================");

        return Result.success(resultMap);
    }

    /**
     * Google 登录 - 跳转到 Google 授权页
     */
    @GetMapping("/user/google/login")
    public void googleLogin(HttpServletResponse response) throws IOException {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                + "&access_type=offline";
        response.sendRedirect(authUrl);
    }

    /**
     * Google 登录回调 - 用 code 换取用户信息，完成登录/注册
     */
    @GetMapping("/user/google/callback")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        try {
            // 1. 用 code 换取 access_token
            Map<String, Object> tokenParams = new HashMap<>();
            tokenParams.put("code", code);
            tokenParams.put("client_id", googleClientId);
            tokenParams.put("client_secret", googleClientSecret);
            tokenParams.put("redirect_uri", googleRedirectUri);
            tokenParams.put("grant_type", "authorization_code");

            String tokenResponse = HttpUtil.post("https://oauth2.googleapis.com/token", tokenParams);
            JSONObject tokenJson = JSONUtil.parseObj(tokenResponse);
            String accessToken = tokenJson.getStr("access_token");

            if (accessToken == null) {
                response.sendRedirect(frontendUrl + "/login?error=" +
                        URLEncoder.encode("Google授权失败", StandardCharsets.UTF_8));
                return;
            }

            // 2. 用 access_token 获取用户信息
            String userInfoResponse = HttpUtil.createGet("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .execute()
                    .body();
            JSONObject googleUser = JSONUtil.parseObj(userInfoResponse);

            String googleId = googleUser.getStr("id");
            String email = googleUser.getStr("email");
            String name = googleUser.getStr("name");
            String picture = googleUser.getStr("picture");

            if (googleId == null) {
                response.sendRedirect(frontendUrl + "/login?error=" +
                        URLEncoder.encode("获取Google用户信息失败", StandardCharsets.UTF_8));
                return;
            }

            // 3. 查找或创建用户
            User user = userService.getOne(new QueryWrapper<User>().eq("google_id", googleId));

            if (user == null) {
                // 新用户，自动注册
                user = new User();
                user.setId(IdUtil.getSnowflakeNextId());
                user.setGoogleId(googleId);
                user.setGoogleEmail(email);
                user.setUsername("google_" + googleId.substring(0, Math.min(googleId.length(), 10)));
                user.setPassword(PasswordUtil.encrypt(IdUtil.simpleUUID())); // 随机密码
                user.setNickname(name != null ? name : "Google用户");
                user.setEmail(email);
                user.setAvatar(picture != null ? picture : "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
                user.setType(0);
                user.setStatus(0);
                user.setCreateTime(new Date());
                userService.save(user);
            }

            // 4. 检查账号状态
            if (user.getStatus() != null && user.getStatus() == -1) {
                response.sendRedirect(frontendUrl + "/login?error=" +
                        URLEncoder.encode("账号已被禁用", StandardCharsets.UTF_8));
                return;
            }

            // 5. 生成 token 并存入 session
            String token = IdUtil.simpleUUID();
            sessionMap.put(token, user);

            // 6. 重定向到前端，带上 token
            response.sendRedirect(frontendUrl + "/google-callback?token=" + token
                    + "&userInfo=" + URLEncoder.encode(JSONUtil.toJsonStr(buildUserInfoMap(user)), StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(frontendUrl + "/login?error=" +
                    URLEncoder.encode("Google登录失败: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    /**
     * 上传并设置头像
     */
    @PostMapping("/user/uploadAvatar")
    public Result<Object> uploadAvatar(@RequestHeader("accessToken") String token,
                                       @RequestBody Map<String, String> params) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "登录已过期");
        }

        String avatarUrl = params.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            return Result.error("头像URL不能为空");
        }

        User user = userService.getById(currentUser.getId());
        user.setAvatar(avatarUrl);
        user.setAvatarBanned(0);
        userService.updateById(user);

        sessionMap.put(token, user);

        Map<String, String> result = new HashMap<>();
        result.put("avatar", avatarUrl);
        return Result.success(result);
    }

    /**
     * 管理员封禁用户头像
     */
    @PostMapping("/user/admin/banAvatar/{id}")
    public Result<Object> banAvatar(@PathVariable Long id,
                                    @RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权操作");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        String defaultAvatar = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";
        user.setAvatar(defaultAvatar);
        user.setAvatarBanned(1);
        userService.updateById(user);

        // 同步更新 session 中的用户信息
        sessionMap.forEach((k, v) -> {
            if (v.getId().equals(id)) {
                v.setAvatar(defaultAvatar);
                v.setAvatarBanned(1);
            }
        });

        Notification notification = new Notification();
        notification.setId(IdUtil.getSnowflakeNextId());
        notification.setUserId(id);
        notification.setTitle("头像违规通知");
        notification.setContent("您的头像因不符合社区规范已被管理员重置为默认头像，请上传合规的头像。");
        notification.setType(1);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        notificationService.save(notification);

        return Result.success("已封禁用户头像并发送通知");
    }

    /**
     * 获取当前用户的通知列表
     */
    @GetMapping("/user/notifications")
    public Result<List<Notification>> getNotifications(@RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "登录已过期");
        }

        QueryWrapper<Notification> qw = new QueryWrapper<>();
        qw.eq("user_id", currentUser.getId());
        qw.orderByDesc("create_time");
        qw.last("LIMIT 50");

        return Result.success(notificationService.list(qw));
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/user/notifications/read/{id}")
    public Result<Object> markNotificationRead(@PathVariable Long id,
                                               @RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "登录已过期");
        }

        Notification notification = notificationService.getById(id);
        if (notification == null || !notification.getUserId().equals(currentUser.getId())) {
            return Result.error("通知不存在");
        }

        notification.setIsRead(1);
        notificationService.updateById(notification);

        return Result.success("已标记为已读");
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/user/notifications/readAll")
    public Result<Object> markAllNotificationsRead(@RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "登录已过期");
        }

        notificationService.update(
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Notification>()
                        .eq("user_id", currentUser.getId())
                        .eq("is_read", 0)
                        .set("is_read", 1)
        );

        return Result.success("已全部标记为已读");
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/user/notifications/unreadCount")
    public Result<Long> getUnreadCount(@RequestHeader("accessToken") String token) {
        User currentUser = sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "登录已过期");
        }

        long count = notificationService.count(
                new QueryWrapper<Notification>()
                        .eq("user_id", currentUser.getId())
                        .eq("is_read", 0)
        );

        return Result.success(count);
    }
}
