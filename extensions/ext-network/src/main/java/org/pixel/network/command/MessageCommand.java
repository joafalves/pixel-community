package org.pixel.network.command;

import lombok.Builder;
import lombok.Getter;

import java.io.Serial;

@Builder
@Getter
public class MessageCommand extends Command {

    @Serial
    private static final long serialVersionUID = 1L;
    private String message;


}
