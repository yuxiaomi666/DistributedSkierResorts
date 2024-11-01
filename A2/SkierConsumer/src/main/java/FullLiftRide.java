/*
 * Ski Data API for NEU Seattle distributed systems course
 * An API for an emulation of skier managment system for RFID tagged lift tickets. Basis for CS6650 Assignments for 2019
 *
 * OpenAPI spec version: 2.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

//package io.swagger.client.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * LiftRide
 */


public class FullLiftRide {
    @SerializedName("time")
    private Integer time = null;

    @SerializedName("liftID")
    private Integer liftID = null;

    @SerializedName("resortID")
    private Integer resortID;

    @SerializedName("seasonID")
    private Integer seasonID;

    @SerializedName("dayID")
    private Integer dayID;

    @SerializedName("skierID")
    private Integer skierID;

    public FullLiftRide time(Integer time) {
        this.time = time;
        return this;
    }
    /**
     * Get time
     * @return time
     **/
    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public FullLiftRide liftID(Integer liftID) {
        this.liftID = liftID;
        return this;
    }
    /**
     * Get liftID
     * @return liftID
     **/
    public Integer getLiftID() {
        return liftID;
    }

    public void setLiftID(Integer liftID) {
        this.liftID = liftID;
    }

    public FullLiftRide resortID(Integer resortID) {
        this.resortID = resortID;
        return this;
    }
    /**
     * Get resortID
     * @return resortID
     **/
    public Integer getResortID() {
        return resortID;
    }

    public void setResortID(Integer resortID) {
        this.resortID = resortID;
    }

    public FullLiftRide seasonID(Integer seasonID) {
        this.seasonID = seasonID;
        return this;
    }
    /**
     * Get seasonID
     * @return seasonID
     **/
    public Integer getSeasonID() {
        return seasonID;
    }

    public void setSeasonID(Integer seasonID) {
        this.seasonID = seasonID;
    }

    public FullLiftRide dayID(Integer dayID) {
        this.dayID = dayID;
        return this;
    }
    /**
     * Get dayID
     * @return dayID
     **/
    public Integer getDayID() {
        return dayID;
    }

    public void setDayID(Integer dayID) {
        this.dayID = dayID;
    }

    public FullLiftRide skierID(Integer skierID) {
        this.skierID = skierID;
        return this;
    }

    /**
     * Get skierID
     * @return skierID
     **/
    public Integer getSkierID() {
        return skierID;
    }

    public void setSkierID(Integer skierID) {
        this.skierID = skierID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FullLiftRide liftRide = (FullLiftRide) o;
        return Objects.equals(time, liftRide.time) &&
                Objects.equals(liftID, liftRide.liftID) &&
                Objects.equals(resortID, liftRide.resortID) &&
                Objects.equals(seasonID, liftRide.seasonID) &&
                Objects.equals(dayID, liftRide.dayID) &&
                Objects.equals(skierID, liftRide.skierID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, liftID, resortID, seasonID, dayID, skierID);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LiftRide {\n");
        sb.append("    time: ").append(toIndentedString(time)).append("\n");
        sb.append("    liftID: ").append(toIndentedString(liftID)).append("\n");
        sb.append("    resortID: ").append(toIndentedString(resortID)).append("\n");
        sb.append("    seasonID: ").append(toIndentedString(seasonID)).append("\n");
        sb.append("    dayID: ").append(toIndentedString(dayID)).append("\n");
        sb.append("    skierID: ").append(toIndentedString(skierID)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
