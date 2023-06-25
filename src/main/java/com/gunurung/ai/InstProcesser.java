package com.gunurung.ai;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;


public class InstProcesser {
    final static int shuffleSeed = 42;

    public void initData(double[][] musics, double[][] unaccompanied){

        /*
            musics 는 0~1 값 안에 정규화된 시간-진폭값들을 double[].length 의 미니배치 사이즈개로 나눠둔 array 이다.
            unaccompanied 는 musics 에서 한개의 악기만을 추출한 결과값으로서의 array 이다.
         */
        INDArray input = Nd4j.create(musics);
        INDArray output = Nd4j.create(unaccompanied);
        DataSet dataSet = new DataSet(input,output);
        dataSet.shuffle(shuffleSeed);

        /*
            dataSet 을 정규분포로 변환 https://recordsoflife.tistory.com/219 참고 뭔소린지 이해못함
         */
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(dataSet);
        normalizer.transform(dataSet);

        /*
            dataSet 을 학습용과 테스트용으로 분리, 아래에선 85%를 학습에 15%를 테스트에.
         */
        SplitTestAndTrain testAndTrain = dataSet.splitTestAndTrain(0.85);
        DataSet trainingData = testAndTrain.getTrain();
        DataSet testData = testAndTrain.getTest();

    }
}
