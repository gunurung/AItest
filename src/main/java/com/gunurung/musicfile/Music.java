package com.gunurung.musicfile;

import com.gunurung.musicfile.ByteConverter;
import org.datavec.api.writable.Writable;
import org.datavec.api.writable.WritableType;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class Music implements Writable {
    public final static int dataLen = 30000000;
    public final static int sampleRate = 40000;
    public final static byte channels = 1;
    public final static byte bits = 16;
    static byte[] header = new byte[44];
    {
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = ByteConverter.toLittleEndian_4(dataLen*2+36)[0];
        header[5] = ByteConverter.toLittleEndian_4(dataLen*2+36)[1];
        header[6] = ByteConverter.toLittleEndian_4(dataLen*2+36)[2];
        header[7] = ByteConverter.toLittleEndian_4(dataLen*2+36)[3];
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 1;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = channels;
        header[23] = 0;
        header[24] = ByteConverter.toLittleEndian_4(sampleRate)[0];
        header[25] = ByteConverter.toLittleEndian_4(sampleRate)[1];
        header[26] = ByteConverter.toLittleEndian_4(sampleRate)[2];
        header[27] = ByteConverter.toLittleEndian_4(sampleRate)[3];
        header[28] = ByteConverter.toLittleEndian_4(sampleRate*channels*bits)[0];
        header[29] = ByteConverter.toLittleEndian_4(sampleRate*channels*bits)[1];
        header[30] = ByteConverter.toLittleEndian_4(sampleRate*channels*bits)[2];
        header[31] = ByteConverter.toLittleEndian_4(sampleRate*channels*bits)[3];
        header[32] = (byte) (channels*bits);
        header[33] = 0;
        header[34] = bits;
        header[35] = 0;
        header[36] = 'd';//'d';
        header[37] = 'a';//'a';
        header[38] = 't';//'t';
        header[39] = 'a';//'a';
        header[40] = ByteConverter.toLittleEndian_4(dataLen)[0];
        header[41] = ByteConverter.toLittleEndian_4(dataLen)[1];
        header[42] = ByteConverter.toLittleEndian_4(dataLen)[2];
        header[43] = ByteConverter.toLittleEndian_4(dataLen)[3];
    }
    short[] amps;
    public Music(URL url)  {
        try {
            amps = getWavAmplitudes(new File(url.toURI()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public double[] normalizedValue(){
        double[] normalized = new double[dataLen*2];
        for(int i = 0; i < dataLen*2; i++){
            normalized[i] = ((int)amps[i]+32767)/65536.0;
        }
        return normalized;
    }
    public byte[] mergeByte(byte[] b1, byte[] b2){
        byte[] b3 = new byte[b1.length+b2.length];
        for(int i = 0; i < b1.length; i++)
            b3[i] = b1[i];
        for(int i = 0; i < b2.length; i++)
            b3[b1.length + i] = b2[i];
        return b3;
    }

    public void playSound() throws UnsupportedAudioFileException, IOException {
        byte[] pcm = new byte[amps.length*2];
        for(int i = 0; i < amps.length; i++) {
            byte[] b = ByteConverter.toLittleEndian_2(amps[i]);
            pcm[i * 2] = b[0];
            pcm[i*2+1] = b[1];
        }

        InputStream input = new ByteArrayInputStream(mergeByte(header,pcm));
        input.reset();
        AudioInputStream ais = AudioSystem.getAudioInputStream(input);
        AudioFormat audioFormat = ais.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
            sourceLine.start();
            int nBytesRead = 0;
            byte[] abData = new byte[128000];
            while (nBytesRead != -1) {
                try {
                    nBytesRead = ais.read(abData, 0, abData.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (nBytesRead >= 0) {
                    @SuppressWarnings("unused")
                    int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
                }
            }
            sourceLine.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        ais.close();
    }
    private short[] getWavAmplitudes(File file) throws IOException {

        //Get Audio input stream
        try (AudioInputStream input = AudioSystem.getAudioInputStream(file)) {
            AudioFormat baseFormat = input.getFormat();
            //Encoding
            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_UNSIGNED;
            float audioSR = baseFormat.getSampleRate();
            int audioCH = baseFormat.getChannels();
            int audioBits = baseFormat.getSampleSizeInBits();
            AudioFormat decodedFormat = new AudioFormat(encoding, audioSR, audioBits, audioCH, audioCH * audioBits / 8, audioSR, false);
            int available = input.available();
            //Get the PCM Decoded Audio Input Stream
            try (AudioInputStream pcmDecodedInput = AudioSystem.getAudioInputStream(decodedFormat, input)) {
                final int BUFFER_SIZE = 2; //this is actually bytes

                //Create a buffer
                byte[] buffer = new byte[BUFFER_SIZE];

                //Now get the average to a smaller array
                int maximumArrayLength = available;
                short[] finalAmplitudes = new short[maximumArrayLength];
                byte[] b = new byte[2];
                //Variables to calculate finalAmplitudes array
                int arrayCellPosition = 0;

                //Read all the available data on chunks

                while (pcmDecodedInput.readNBytes(buffer, 0, BUFFER_SIZE) > 0)
                    for (int i = 0; i < buffer.length-1; i+=2) {
                        finalAmplitudes[arrayCellPosition++] = ByteConverter.fromLittleEndian_2(buffer,i);
                    }
                System.out.println("size" + finalAmplitudes.length);
                return finalAmplitudes;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();

        }

        //You don't want this to reach here...
        return new short[1];
    }

    @Override
    public void write(DataOutput out) throws IOException {

    }

    @Override
    public void readFields(DataInput in) throws IOException {

    }

    @Override
    public void writeType(DataOutput out) throws IOException {

    }

    @Override
    public double toDouble() {
        return 0;
    }

    @Override
    public float toFloat() {
        return 0;
    }

    @Override
    public int toInt() {
        return 0;
    }

    @Override
    public long toLong() {
        return 0;
    }

    @Override
    public WritableType getType() {
        return null;
    }
}
