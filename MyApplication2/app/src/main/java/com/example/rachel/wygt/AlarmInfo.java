package com.example.rachel.wygt;

import android.util.Log;

/**
 * Created by Rachel on 11/25/14.
 */
public class AlarmInfo {

    public final int ALARM_TYPE_DAILY = 0;
    public final int ALARM_TYPE_SELECT_DAYS = 1;
    public final int ALARM_TYPE_ONCE = 2;


    // false == 0
    // true == 1

    private int mon, tue, wed, thu, fri, sat, sun, alarmType;
    private boolean _mon, _tue, _wed, _thu, _fri, _sat,_sun;
    private long taskId, id;

    public AlarmInfo(){
        _mon = false;
        _tue = false;
        _wed = false;
        _thu = false;
        _fri = false;
        _sat = false;
        _sun = false;
    }

    public int getMon() {
        return mon;
    }

    public void setMon(int mon) {
        this.mon = mon;
        if(mon == 0){
            _mon = false;
        }
        else if(mon == 1){
            _mon = true;
        }
        else{
            Log.d("Alarm Info", "Something is wrong - Monday");
        }
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskNum(long taskNum) {
        this.taskId = taskNum;
    }

    public int getTue() {
        return tue;
    }

    public void setTue(int tue) {
        this.tue = tue;
        if(tue == 0){
            _tue = false;
        }
        else if(tue == 1){
            _tue = true;
        }
        else{
            Log.d("Alarm Info", "Something is wrong - Tuesday");
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWed() {
        return wed;
    }

    public void setWed(int wed) {
        this.wed = wed;
        if(wed == 0){
            _wed = false;
        }
        else if(wed == 1){
            _wed = true;
        }
        else{
            Log.d("Alarm Info", "Something is wrong - Wednesday");
        }
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
        if(thu == 0){
            _thu = false;
        }
        else if(thu == 1){
            _thu = true;
        }
        else{
            Log.d("Alarm Info", "Something is wrong - Thursday");
        }
    }

    public int getFri() {
        return fri;
    }

    public void setFri(int fri) {
        this.fri = fri;
        if(fri == 0){
            _fri = false;
        }
        else if(fri == 1){
            _fri = true;
        }
        else{
            Log.d("Alarm Info", "Something is wrong - Friday");
        }
    }

    public int getSat() {
        return sat;
    }

    public void setSat(int sat) {
        this.sat = sat;
        if(sat == 0){
            _sat = false;
        }
        else if(sat == 1){
            _sat = true;
        }
        else{
            Log.d("Alarm Info", "Something is wrong - Tuesday");
        }
    }

    public int getSun() {
        return sun;
    }

    public void setSun(int sun) {
        this.sun = sun;

        if(sun == 0){
            _sun = false;
        }
        else if(sun == 1){
            _sun = true;
        }
        else{
            Log.d("Alarm Info", "Something is wrong - Tuesday");
        }
    }

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public int getALARM_TYPE_DAILY() {
        return ALARM_TYPE_DAILY;
    }

    public int getALARM_TYPE_SELECT_DAYS() {
        return ALARM_TYPE_SELECT_DAYS;
    }

    public int getALARM_TYPE_ONCE() {
        return ALARM_TYPE_ONCE;
    }

    public boolean is_mon() {
        return _mon;
    }

    public boolean is_tue() {
        return _tue;
    }

    public boolean is_wed() {
        return _wed;
    }

    public boolean is_thu() {
        return _thu;
    }

    public boolean is_fri() {
        return _fri;
    }

    public boolean is_sat() {
        return _sat;
    }

    public boolean is_sun() {
        return _sun;
    }


    public void set_mon(boolean _mon) {
        this._mon = _mon;
        if(_mon = true){
            mon = 1;
        }
        else{
            mon = 0;
        }
    }

    public void set_tue(boolean _tue) {
        this._tue = _tue;
        if(_tue = true){
            tue = 1;
        }
        else{
            tue = 0;
        }
    }

    public void set_wed(boolean _wed) {
        this._wed = _wed;
        if(_wed = true){
            wed = 1;
        }
        else{
            wed = 0;
        }
    }

    public void set_thu(boolean _thu) {
        this._thu = _thu;
        if(_thu = true){
            thu = 1;
        }
        else{
            thu = 0;
        }
    }

    public void set_fri(boolean _fri) {
        this._fri = _fri;
        if(_fri = true){
            fri = 1;
        }
        else{
            fri = 0;
        }
    }

    public void set_sat(boolean _sat) {
        this._sat = _sat;
        if(_sat = true){
            sat = 1;
        }
        else{
            sat = 0;
        }
    }

    public void set_sun(boolean _sun) {
        this._sun = _sun;
        if(_sun = true){
            sun = 1;
        }
        else{
            sun = 0;
        }
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }
}
