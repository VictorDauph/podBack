package com.pod.back.printful.dto;

// Couvre toute la surface imprimable du printfile (top/left à 0, width/height = taille du printfile).
public record MockupPosition(int areaWidth, int areaHeight, int width, int height, int top, int left) {
}
