package jnpf.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jnpf.base.ActionResult;
import jnpf.base.entity.EmailConfigEntity;
import jnpf.base.entity.EmailReceiveEntity;
import jnpf.base.model.MailAccount;
import jnpf.base.service.SysconfigService;
import jnpf.base.util.Pop3Util;
import jnpf.base.vo.PaginationVO;
import jnpf.entity.EmailSendEntity;
import jnpf.exception.DataException;
import jnpf.model.email.EmailCheckForm;
import jnpf.model.email.EmailCofigInfoVO;
import jnpf.model.email.EmailCrForm;
import jnpf.model.email.EmailDraftListVO;
import jnpf.model.email.EmailInfoVO;
import jnpf.model.email.EmailReceiveListVO;
import jnpf.model.email.EmailSendCrForm;
import jnpf.model.email.EmailSentListVO;
import jnpf.model.email.EmailStarredListVO;
import jnpf.model.email.PaginationEmail;
import jnpf.service.EmailReceiveService;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 邮件配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2019年9月26日 上午9:18
 */
@Tag(name = "邮件收发", description = "Email")
@RestController
@RequestMapping("/api/extend/Email")
public class EmailController {

    @Autowired
    private EmailReceiveService emailReceiveService;
    @Autowired
    private Pop3Util pop3Util;
    @Autowired
    private SysconfigService sysconfigService;

    /**
     * 获取邮件列表(收件箱、标星件、草稿箱、已发送)
     *
     * @param paginationEmail 分页模型
     * @return
     */
    @Operation(summary = "获取邮件列表(收件箱、标星件、草稿箱、已发送)")
    @GetMapping
    @SaCheckPermission("extend.email")
    public ActionResult receiveList(PaginationEmail paginationEmail) {
        String type = paginationEmail.getType() != null ? paginationEmail.getType() : "inBox";
        switch (type) {
            case "inBox":
                List<EmailReceiveEntity> entity = emailReceiveService.getReceiveList(paginationEmail);
                PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationEmail, PaginationVO.class);
                List<EmailReceiveListVO> listVO = JsonUtil.getJsonToList(entity, EmailReceiveListVO.class);
                return ActionResult.page(listVO,paginationVO);
            case "star":
                List<EmailReceiveEntity> entity1 = emailReceiveService.getStarredList(paginationEmail);
                PaginationVO paginationVo1 = JsonUtil.getJsonToBean(paginationEmail, PaginationVO.class);
                List<EmailStarredListVO> listVo1 = JsonUtil.getJsonToList(entity1, EmailStarredListVO.class);
                return ActionResult.page(listVo1,paginationVo1);
            case "draft":
                List<EmailSendEntity> entity2 = emailReceiveService.getDraftList(paginationEmail);
                PaginationVO paginationVo2 = JsonUtil.getJsonToBean(paginationEmail, PaginationVO.class);
                List<EmailDraftListVO> listVo2 = JsonUtil.getJsonToList(entity2, EmailDraftListVO.class);
                return ActionResult.page(listVo2,paginationVo2);
            case "sent":
                List<EmailSendEntity> entity3 = emailReceiveService.getSentList(paginationEmail);
                PaginationVO paginationVo3 = JsonUtil.getJsonToBean(paginationEmail, PaginationVO.class);
                List<EmailSentListVO> listVo3 = JsonUtil.getJsonToList(entity3, EmailSentListVO.class);
                return ActionResult.page(listVo3,paginationVo3);
            default:
                return ActionResult.fail("获取失败");
        }
    }

    /**
     * 获取邮箱配置
     *
     * @return
     */
    @Operation(summary = "获取邮箱配置")
    @GetMapping("/Config")
    @SaCheckPermission("extend.email")
    public ActionResult<EmailCofigInfoVO> configInfo() {
        EmailConfigEntity entity = emailReceiveService.getConfigInfo();
        EmailCofigInfoVO vo = JsonUtil.getJsonToBean(entity, EmailCofigInfoVO.class);
        if(vo==null){
            vo=new EmailCofigInfoVO();
        }
        return ActionResult.success(vo);
    }

    /**
     * 获取邮件信息
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "获取邮件信息")
    @GetMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult<EmailInfoVO> info(@PathVariable("id") String id) throws DataException {
        Object entity = emailReceiveService.getInfo(id);
        EmailInfoVO vo = JsonUtil.getJsonToBeanEx(entity, EmailInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 删除
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "删除邮件")
    @DeleteMapping("/{id}")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult delete(@PathVariable("id") String id) {
        boolean flag= emailReceiveService.delete(id);
        if(flag==false){
            return ActionResult.fail("删除失败，邮件不存在");
        }
        return ActionResult.success("删除成功");
    }

    /**
     * 设置已读邮件
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "设置已读邮件")
    @PutMapping("/{id}/Actions/Read")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult receiveRead(@PathVariable("id") String id) {
        boolean flag= emailReceiveService.receiveRead(id, 1);
        if(flag==false){
            return ActionResult.fail("操作失败，邮件不存在");
        }
        return ActionResult.success("操作成功");
    }

    /**
     * 设置未读邮件
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "设置未读邮件")
    @PutMapping("/{id}/Actions/Unread")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult receiveUnread(@PathVariable("id") String id) {
        boolean flag= emailReceiveService.receiveRead(id, 0);
        if(flag==false){
            return ActionResult.fail("操作失败，邮件不存在");
        }
        return ActionResult.success("操作成功");
    }

    /**
     * 设置星标邮件
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "设置星标邮件")
    @PutMapping("/{id}/Actions/Star")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult receiveYesStarred(@PathVariable("id") String id) {
        boolean flag= emailReceiveService.receiveStarred(id, 1);
        if(flag==false){
            return ActionResult.fail("操作失败，邮件不存在");
        }
        return ActionResult.success("操作成功");
    }

    /**
     * 设置取消星标
     *
     * @param id 主键
     * @return
     */
    @Operation(summary = "设置取消星标")
    @PutMapping("/{id}/Actions/Unstar")
    @Parameters({
            @Parameter(name = "id", description = "主键",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult receiveNoStarred(@PathVariable("id") String id) {
        boolean flag= emailReceiveService.receiveStarred(id, 0);
        if(flag==false){
            return ActionResult.fail("操作失败，邮件不存在");
        }
        return ActionResult.success("操作成功");
    }

    /**
     * 收邮件
     *
     * @return
     */
    @Operation(summary = "收邮件")
    @PostMapping("/Receive")
    @SaCheckPermission("extend.email")
    public ActionResult receive() {
        EmailConfigEntity configEntity = emailReceiveService.getConfigInfo();
        if (configEntity != null) {
            MailAccount mailAccount = new MailAccount();
            mailAccount.setAccount(configEntity.getAccount());
            mailAccount.setPassword(configEntity.getPassword());
            mailAccount.setPop3Host(configEntity.getPop3Host());
            mailAccount.setPop3Port(configEntity.getPop3Port());
            mailAccount.setSmtpHost(configEntity.getSmtpHost());
            mailAccount.setSmtpPort(configEntity.getSmtpPort());
            if ("1".equals(String.valueOf(configEntity.getEmailSsl()))) {
                mailAccount.setSsl(true);
            } else {
                mailAccount.setSsl(false);
            }
            boolean checkResult=pop3Util.checkConnected(mailAccount);
            if (checkResult) {
                int mailCount = emailReceiveService.receive(configEntity);
                return ActionResult.success("操作成功", mailCount);
            } else {
                return ActionResult.fail("账户认证错误");
            }
        } else {
            return ActionResult.fail("你还没有设置邮件的帐户");
        }
    }

    /**
     * 存草稿
     *
     * @param emailSendCrForm 邮件模型
     * @return
     */
    @Operation(summary = "存草稿")
    @PostMapping("/Actions/SaveDraft")
    @Parameters({
            @Parameter(name = "emailSendCrForm", description = "邮件模型",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult saveDraft(@RequestBody @Valid EmailSendCrForm emailSendCrForm) {
        EmailSendEntity entity = JsonUtil.getJsonToBean(emailSendCrForm, EmailSendEntity.class);
        emailReceiveService.saveDraft(entity);
        return ActionResult.success("保存成功");
    }

    /**
     * 发邮件
     *
     * @param emailCrForm 发送邮件模型
     * @return
     */
    @Operation(summary = "发邮件")
    @PostMapping
    @Parameters({
            @Parameter(name = "emailCrForm", description = "发送邮件模型",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult saveSent(@RequestBody @Valid EmailCrForm emailCrForm) {
        EmailSendEntity entity = JsonUtil.getJsonToBean(emailCrForm, EmailSendEntity.class);
        EmailConfigEntity configEntity = emailReceiveService.getConfigInfo();
        if (configEntity != null) {
            MailAccount mailAccount = new MailAccount();
            mailAccount.setAccount(configEntity.getAccount());
            mailAccount.setPassword(configEntity.getPassword());
            mailAccount.setPop3Host(configEntity.getPop3Host());
            mailAccount.setPop3Port(configEntity.getPop3Port());
            mailAccount.setSmtpHost(configEntity.getSmtpHost());
            mailAccount.setSmtpPort(configEntity.getSmtpPort());
            if ("1".equals(String.valueOf(configEntity.getEmailSsl()))) {
                mailAccount.setSsl(true);
            } else {
                mailAccount.setSsl(false);
            }
            int flag = emailReceiveService.saveSent(entity, configEntity);
            if (flag == 0) {
                return ActionResult.success("发送成功");
            } else {
                return ActionResult.fail("账户认证错误");
            }
        } else {
            return ActionResult.fail("你还没有设置邮件的帐户");
        }
    }

    /**
     * 更新邮件配置
     *
     * @param emailCheckForm 邮件配置模型
     * @return
     */
    @Operation(summary = "更新邮件配置")
    @PutMapping("/Config")
    @Parameters({
            @Parameter(name = "emailCheckForm", description = "邮件配置模型",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult saveConfig(@RequestBody @Valid EmailCheckForm emailCheckForm) throws DataException {
        EmailConfigEntity entity = JsonUtil.getJsonToBean(emailCheckForm, EmailConfigEntity.class);
        emailReceiveService.saveConfig(entity);
        return ActionResult.success("保存成功");
    }

    /**
     * 邮箱配置-测试连接
     *
     * @param emailCheckForm 邮件配置模型
     * @return
     */
    @Operation(summary = "邮箱配置-测试连接")
    @PostMapping("/Config/Actions/CheckMail")
    @Parameters({
            @Parameter(name = "emailCheckForm", description = "邮件配置模型",required = true),
    })
    @SaCheckPermission("extend.email")
    public ActionResult checkLogin(@RequestBody @Valid EmailCheckForm emailCheckForm) {
        EmailConfigEntity entity = JsonUtil.getJsonToBean(emailCheckForm, EmailConfigEntity.class);
        boolean result = emailReceiveService.checkLogin(entity);
        if (result) {
            return ActionResult.success("验证成功");
        } else {
            return ActionResult.fail("账户认证错误");
        }
    }

    /**
     * 列表（收件箱）
     *
     * @return
     */
    @GetMapping("/GetReceiveList")
    public List<EmailReceiveEntity> getReceiveList(){
        return emailReceiveService.getDashboardReceiveList();
    }

}
