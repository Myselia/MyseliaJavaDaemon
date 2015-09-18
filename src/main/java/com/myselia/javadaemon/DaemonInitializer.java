package com.myselia.javadaemon;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import com.myselia.javacommon.communication.codecs.StringToTransmissionDecoder;
import com.myselia.javacommon.communication.codecs.TransmissionToStringEncoder;

public class DaemonInitializer extends ChannelInitializer<SocketChannel> {

	private static final int MAX_FRAME_SIZE = 65565;
	private final StringDecoder stringDecoder = new StringDecoder(
			CharsetUtil.UTF_8);
	private final StringEncoder stringEncoder = new StringEncoder(
			CharsetUtil.UTF_8);

	public DaemonInitializer() {
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		/*
		 * CODECS
		 */

		// Decoders
		pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(
				MAX_FRAME_SIZE, true, Delimiters.lineDelimiter()));
		pipeline.addLast("stringDecoder", stringDecoder);
		pipeline.addLast("transmissionDecoder", new StringToTransmissionDecoder());

		// Encoders
		pipeline.addLast("stringEncoder", stringEncoder);
		pipeline.addLast("transmissionEncoder", new TransmissionToStringEncoder());

		/*
		 * APPLICATION BUSINESS LOGIC
		 */

		// Stem Session
		pipeline.addLast("session", new SlaveSession(ch));
	}

}
