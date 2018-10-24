/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2014 Oleg Trifonov
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.viewer.audio;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.viewer.FileViewer;
//import com.xuggle.xuggler.*;
import org.fife.ui.StatusBar;

import javax.sound.sampled.*;
import java.io.IOException;



public class AudioViewer extends FileViewer {

    /**
     * The audio line we'll output sound to; it'll be the default audio device on your system if available
     */
    private static SourceDataLine mLine;

    @Override
    protected void show(AbstractFile file) {
        setComponentToPresent(new AudioPlayer());
        System.out.println("PLAY " + file.getURL().toString());

//        ToolFactory.setTurboCharged(true);
//        IMediaReader reader = ToolFactory.makeReader(file.getAbsolutePath());
//        reader.addListener(ToolFactory.makeViewer(IMediaViewer.Mode.AUDIO_ONLY));
//        while (reader.readPacket() == null) {
//System.out.println('.');
//            do {} while(false);
//        }
//        play(file.getAbsolutePath());
        System.out.println("STOP " + file.getURL().toString());


    }


//    private void play(String filename) {
//        // create a Xuggler container object
//        IContainer container = IContainer.make();
//
//        // Open up the container
//        if (container.open(filename, IContainer.Type.READ, null) < 0) {
//            throw new IllegalArgumentException("could not open file: " + filename);
//        }
//
//        // query how many streams the call to open found
//        int numStreams = container.getNumStreams();
//System.out.println("numStreams " + numStreams);
//        // and iterate through the streams to find the first audio stream
//        int audioStreamId = -1;
//        IStreamCoder audioCoder = null;
//        for (int i = 0; i < numStreams; i++) {
//            // Find the stream object
//            IStream stream = container.getStream(i);
//            // Get the pre-configured decoder that can decode this stream;
//            IStreamCoder coder = stream.getStreamCoder();
//
//            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
//                audioStreamId = i;
//                audioCoder = coder;
//                break;
//            }
//        }
//        if (audioStreamId == -1) {
//            throw new RuntimeException("could not find audio stream in container: " + filename);
//        }
//
//        // Now we have found the audio stream in this file.  Let's open up our decoder so it can do work.
//        if (audioCoder.open(null, null  ) < 0) {
//            throw new RuntimeException("could not open audio decoder for container: " + filename);
//        }
//
//        // And once we have that, we ask the Java Sound System to get itself ready.
//        openJavaSound(audioCoder);
//
//        // Now, we start walking through the container looking at each packet.
//        IPacket packet = IPacket.make();
//        while (container.readNextPacket(packet) >= 0) {
//            // Now we have a packet, let's see if it belongs to our audio stream
//            if (packet.getStreamIndex() == audioStreamId) {
//                // We allocate a set of samples with the same number of channels as the coder tells us is in this buffer
//                // We also pass in a buffer size (1024 in our example), although Xuggler will probably allocate more space
//                // than just the 1024 (it's not important why)
//                IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
//
//                // A packet can actually contain multiple sets of samples (or frames of samples
//                // in audio-decoding speak).  So, we may need to call decode audio multiple
//                // times at different offsets in the packet's data.  We capture that here.
//                int offset = 0;
//
//                // Keep going until we've processed all data
//                while (offset < packet.getSize()) {
//                    int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
//                    if (bytesDecoded < 0) {
//                        throw new RuntimeException("got error decoding audio in: " + filename);
//                    }
//                    offset += bytesDecoded;
//
//                    // Some decoder will consume data in a packet, but will not be able to construct a full set of samples yet.
//                    // Therefore you should always check if you got a complete set of samples from the decoder
//                    if (samples.isComplete()) {
//                        playJavaSound(samples);
//                    }
//                }
//            } else {
//                // This packet isn't part of our audio stream, so we just silently drop it.
//                do {} while(false);
//            }
//
//        }
//        // Technically since we're exiting anyway, these will be cleaned up by the garbage collector... but because we're
//        // nice people and want to be invited places for Christmas, we're going to show how to clean up.
//        closeJavaSound();
//
//        if (audioCoder != null) {
//            audioCoder.close();
//            audioCoder = null;
//        }
//        if (container != null) {
//            container.close();
//            container = null;
//        }
//    }
//
//
//    private static void openJavaSound(IStreamCoder aAudioCoder) {
//        AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
//                (int)IAudioSamples.findSampleBitDepth(aAudioCoder.getSampleFormat()),
//                aAudioCoder.getChannels(),
//                true, // xuggler defaults to signed 16 bit samples
//                false);
//        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
//        try {
//            mLine = (SourceDataLine) AudioSystem.getLine(info);
//            // if that succeeded, try opening the line
//            mLine.open(audioFormat);
//            // And if that succeed, start the line
//            mLine.start();
//        } catch (LineUnavailableException e) {
//            throw new RuntimeException("could not open audio line");
//        }
//    }
//
//    private static void playJavaSound(IAudioSamples aSamples) {
//        // We're just going to dump all the samples into the line.
//        byte[] rawBytes = aSamples.getData().getByteArray(0, aSamples.getSize());
//        mLine.write(rawBytes, 0, aSamples.getSize());
//    }

    private static void closeJavaSound() {
        if (mLine != null) {
            // Wait for the line to finish playing
            mLine.drain();
            // Close the line
            mLine.close();
            mLine = null;
        }
    }

    @Override
    protected StatusBar getStatusBar() {
        return new StatusBar();
    }

    @Override
    protected void saveStateOnClose() {

    }

    @Override
    protected void restoreStateOnStartup() {
    }
}
