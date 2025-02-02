package org.pixel.network.command;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HelloCommand extends Command {
    private String username;
    private String password;
}
