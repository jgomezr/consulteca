package org.grameenfoundation.consulteca.io;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */
public class ParcelableString implements Parcelable {
    private String value;

    public ParcelableString() {
    }

    public ParcelableString(String value) {
        this.value = value;
    }

    public ParcelableString(Parcel parcel) {
        this.value = parcel.readString();
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(value);
    }

    /**
     * * This field is needed for Android to be able to * create new objects, individually or as arrays. * * This also means that you can use use the default * constructor to create the object and use another * method to hyrdate it as necessary. * * I just find it easier to use the constructor. * It makes sense for the way my brain thinks ;-) *
     */
    public static final Parcelable.Creator<ParcelableString> CREATOR = new Parcelable.Creator<ParcelableString>() {
        @Override
        public ParcelableString createFromParcel(Parcel in) {
            return new ParcelableString(in);
        }

        @Override
        public ParcelableString[] newArray(int size) {
            return new ParcelableString[size];
        }
    };
}

