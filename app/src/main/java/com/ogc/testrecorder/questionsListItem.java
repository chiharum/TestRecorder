package com.ogc.testrecorder;

public class questionsListItem {

    int questionNumber, correctTimes, challengedTimes;

    public questionsListItem(int questionNumber, int correctTimes, int challengedTimes){
        this.questionNumber = questionNumber;
        this.correctTimes = correctTimes;
        this.challengedTimes = challengedTimes;
    }
}
