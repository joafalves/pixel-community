/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.content.importer;

import org.lwjgl.system.MemoryStack;
import pixel.audio.Sound;
import pixel.commons.logger.Logger;
import pixel.commons.logger.LoggerFactory;
import pixel.content.ContentImporter;
import pixel.content.ContentImporterInfo;
import pixel.content.ImportContext;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.libc.LibCStdlib.free;

/**
 * @author João Filipe Alves
 */
@ContentImporterInfo(type = Sound.class, extension = ".ogg")
public class VorbisAudioImporter implements ContentImporter<Sound> {

    private static final Logger log = LoggerFactory.getLogger(VorbisAudioImporter.class);

    @Override
    public Sound process(ImportContext ctx) {
        int channels;
        int sampleRate;
        ShortBuffer rawAudioBuffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // load the sound resource item:
            IntBuffer channelsBuffer = stack.mallocInt(1);
            IntBuffer sampleRateBuffer = stack.mallocInt(1);

            rawAudioBuffer = stb_vorbis_decode_memory(ctx.getBuffer(), channelsBuffer, sampleRateBuffer);
            if (rawAudioBuffer == null) {
                log.warn("Failed to load sound resource, invalid buffer");
                return null;
            }

            // retrieve the extra information that was stored in the buffers by the function
            channels = channelsBuffer.get(0);
            sampleRate = sampleRateBuffer.get(0);

        } catch (Exception e) {
            log.error("Exception caught: %s", e);
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

        return new Sound(sourcePointer);
    }
}
