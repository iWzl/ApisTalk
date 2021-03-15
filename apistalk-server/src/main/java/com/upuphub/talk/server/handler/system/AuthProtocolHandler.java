package com.upuphub.talk.server.handler.system;

import com.upuphub.talk.server.Environment;
import com.upuphub.talk.server.factory.ProtocolFactory;
import com.upuphub.talk.server.factory.ProtocolPackageFactory;
import com.upuphub.talk.server.handler.HandlerManger;
import com.upuphub.talk.server.handler.HandlerType;
import com.upuphub.talk.server.protocol.*;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register Protocol Process
 *
 * @author Inspiration S.P.A Leo
 * @date create time 2021-03-10 22:37
 **/
public class AuthProtocolHandler extends AbstractSystemProtocolHandler {
    private final Logger logger = LoggerFactory.getLogger(AuthProtocolHandler.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);
    }

    @Override
    CMD command() {
        return CMD.CMD_AUTHORIZATION_REQ;
    }

    @Override
    public void handler(Message<ProtocolPackage> protocolMsg) throws Exception {
        Protocol protocolReq = protocolMsg.body().getProtocol();
        if(Environment.isRegistered(protocolReq.getHeader().getFrom())){
            Protocol protocolRsp = ProtocolFactory.buildAuthorizationRsp(
                    protocolReq.getHeader().getFrom(),MESSAGE_CODE.MC_DUPLICATE,"repeat authentication");
            ProtocolPackage protocolPackage = ProtocolPackageFactory.buildProtocolPackageByProtocolRsp(
                    protocolRsp, HANDLER_CODE.HC_SUCCESS);
            protocolMsg.reply(protocolPackage);
        }else if(HandlerManger.hasHandler(HandlerType.AUTHORIZATION)){
            vertx.eventBus().<ProtocolPackage>request(HandlerType.AUTHORIZATION.getAddress(),protocolMsg.body(),rsp->{
                if(rsp.succeeded() && null != rsp.result()){
                    // 处理成功直接返回用户注册处理器的回包
                    protocolMsg.reply(rsp.result());
                }else {
                    // 处理失败
                    Protocol protocolRsp = ProtocolFactory.buildAuthorizationRsp(
                            protocolReq.getHeader().getFrom(),MESSAGE_CODE.MC_FAILED,"illegal connection");
                    ProtocolPackage protocolPackage = ProtocolPackageFactory.buildProtocolPackageByProtocolRsp(
                            protocolRsp, HANDLER_CODE.HC_UNAUTHORIZED);
                    protocolMsg.reply(protocolPackage);
                }
            });
        }
    }
}
