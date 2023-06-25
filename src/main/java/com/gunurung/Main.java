package com.gunurung;

import com.gunurung.musicfile.Music;

import java.io.*;

import javax.sound.sampled.*;

public class Main {


    public static void main(String[] args) throws UnsupportedAudioFileException, IOException {

        Music music = new Music(Main.class.getClassLoader().getResource("input.wav"));
        music.playSound();
    }
}