package com.upuphub.talk.client.network;

import com.google.protobuf.InvalidProtocolBufferException;
import com.upuphub.talk.client.factory.ProtocolTypeFactory;
import com.upuphub.talk.client.protocol.Protocol;
import com.upuphub.talk.client.protocol.ProtocolPackage;
import com.upuphub.talk.client.util.NumberUtil;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

/**
 * @author Inspiration S.P.A Leo
 * @date create time 2021-03-13 14:58
 **/
public class ProtocolLengthRecordParser {
    private static final Integer FRAME_TOKEN_SIZE = 4;

    public static RecordParser newProtocolParser(NetSocket netSocket, EventBus eventBus){
        RecordParser recordParser = RecordParser.newFixed(FRAME_TOKEN_SIZE);
        recordParser.setOutput(new Handler<Buffer>() {
            // 表示当前数据长度
            FrameToken frameToken = FrameToken.SIZE;
            @Override
            public void handle(Buffer event) {
                switch (frameToken){
                    case SIZE:
                        int frameSize = NumberUtil.byte4ToInt(
                                event.getBuffer(0,FRAME_TOKEN_SIZE).getBytes());
                        // 动态修改长度
                        recordParser.fixedSizeMode(frameSize);
                        frameToken =FrameToken.PAYLOAD;
                        break;
                    case PAYLOAD:
                        // 已经接受到长度信息了，接下来的数据就是protobuf可识别的字节数组
                        byte[] buf = event.getBytes();
                        Protocol protocol = null;
//                        try {
//                            protocol = Protocol.parseFrom(buf);
                            ProtocolPackage protocolPackageReq = ProtocolPackage.newBuilder()
                                    .setSocketHandlerId(netSocket.writeHandlerID())
                                    .build();
//                            eventBus.<ProtocolPackage>request(
//                                    ProtocolTypeFactory.getEventAddressByCmd(protocol.getHeader().getCmd()),
//                                    protocolPackageReq, rsp->{
//                                        if(rsp.succeeded() && null != rsp.result().body()){
//                                            ProtocolPackage protocolPackageRsp = rsp.result().body();
//                                            switch (protocolPackageRsp.getHandlerCode()){
//                                                case HC_SUCCESS:
//                                                    netSocket.write(ProtocolFactory.buildProtocolBuffer(protocolPackageRsp.getProtocol()));
//                                                    break;
//                                                case HC_UNAUTHORIZED:
//                                                    netSocket.close();
//                                                    break;
//                                                case HC_WARNING:
//
//                                                    break;
//                                                case HC_FAILED:
//                                                    netSocket.close();
//                                                    break;
//                                                default:
//                                                    // 不支持的处理类型
//                                                    break;
//                                            }
//                                        }else{
//                                            netSocket.write(ProtocolFactory.buildAuthorizationReq("C","S","T"));
//                                        }
//                                    });
//                        } catch (InvalidProtocolBufferException e) {
//                            netSocket.close();
//                            return;
//                        }
                        // 处理完后要将长度改回
                        recordParser.fixedSizeMode(FRAME_TOKEN_SIZE);
                        // 重置size变量
                        frameToken = FrameToken.SIZE;
                        break;
                    default:
                        break;
                }
            }
        });
        return recordParser;
    }
 }
