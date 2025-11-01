#version 330 core

in vec4 vColor;
in vec2 vTextureCoord;

uniform sampler2D uTextureImage;
uniform float u_glowIntensity;

out vec4 FragColor;

void main() {
    vec4 texColor = texture(uTextureImage, vTextureCoord);
    
    // Calculate brightness of the pixel
    float brightness = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    
    // Create a glow color (bright cyan/blue glow for Earth-like effect)
    vec3 glowColor = vec3(0.4, 0.7, 1.0); // Light blue glow
    
    // Enhance bright areas
    float glowFactor = pow(brightness, 2.0) * u_glowIntensity;
    
    // Add glow to the original color
    vec3 finalColor = texColor.rgb + glowColor * glowFactor;
    
    // Apply saturation boost for more vibrant colors
    vec3 luminance = vec3(0.299, 0.587, 0.114);
    float gray = dot(finalColor, luminance);
    finalColor = mix(vec3(gray), finalColor, 1.0 + u_glowIntensity * 0.3);
    
    FragColor = vec4(finalColor, texColor.a);
}
