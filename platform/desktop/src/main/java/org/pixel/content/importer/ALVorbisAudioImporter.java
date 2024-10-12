/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content.importer;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.system.MemoryStack;
import org.pixel.content.Sound;
import org.pixel.content.openal.ALSound;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentImporter;
import org.pixel.content.ContentImporterInfo;
import org.pixel.content.ImportContext;

@ContentImporterInfo(type = Sound.class, extension = ".ogg")
public class ALVorbisAudioImporter implements ContentImporter<Sound> {

    private static final Logger log = LoggerFactory.getLogger(ALVorbisAudioImporter.class);

    @Override
    public Sound process(ImportContext ctx) {
        int channels;
        int sampleRate;
        ShortBuffer rawAudioBuffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // load the sound resource item:
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);
            ByteBuffer rawBuffer = createByteBuffer(ctx.getData().length);
            rawBuffer.put(ctx.getData()).flip(); // reset position to 0

            rawAudioBuffer = stb_vorbis_decode_memory(rawBuffer, channelsBuffer, sampleRateBuffer);
            if (rawAudioBuffer == null) {
                log.warn("Failed to load sound resource, invalid buffer.");
                return null;
            }

            // retrieve the extra information that was stored in the buffers by the function
            channels = channelsBuffer.get(0);
            sampleRate = sampleRateBuffer.get(0);

        } catch (Exception e) {
            log.error("Exception caught!", e);
            return null;
        }

        // assign the correct format:
        int format = -1;
        if (channels == 1) {
            format = AL_FORMAT_MONO16;

        } else if (channels == 2) {
            format = AL_FORMAT_STEREO16;
        }

        // request space for buffer:
        int bufferPointer = alGenBuffers();

        // send the sound data to OpenAl:
        alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

        // free stb allocated memory
        free(rawAudioBuffer);

        // assign loaded resource to a source:
        int sourcePointer = alGenSources();
        alSourcei(sourcePointer, AL_BUFFER, bufferPointer);

        return new ALSound(sourcePointer);
    }
}
