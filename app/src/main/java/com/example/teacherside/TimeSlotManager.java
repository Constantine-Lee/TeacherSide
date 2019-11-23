package com.example.teacherside;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotManager {

    private List<TimeSlot> mTimeSlots;
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private List<String> keyList = new ArrayList<String>();
    private Context mContext;
    private String mPath, mDate;
    private UpdateInfo mUpdateInfo;

    public interface UpdateInfo{
        void UpdateInfo(String date);
        void NotifyDataInserted(int position);
        void NotifyDataRemoved(int position);
    }

    public TimeSlotManager(Context context, String path) {
        mPath = path;
        mContext = context.getApplicationContext();
        mTimeSlots = new ArrayList<>();
        if(context instanceof UpdateInfo){
            mUpdateInfo = (UpdateInfo) context;
        }

        mFirebaseDatabase.getReference("units").child(mPath).child("timeslots").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mTimeSlots.add((TimeSlot) dataSnapshot.getValue(TimeSlot.class));
                keyList.add(dataSnapshot.getKey());
                if(mUpdateInfo!=null) {
                    mUpdateInfo.NotifyDataInserted(mTimeSlots.size()-1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                int position = keyList.indexOf(dataSnapshot.getKey());
                mTimeSlots.set(position, (TimeSlot) dataSnapshot.getValue(TimeSlot.class));
                TimeSlotsInterface.NotifyAdapterDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                int position = keyList.indexOf(dataSnapshot.getKey());
                mTimeSlots.remove(position);
                keyList.remove(dataSnapshot.getKey());
                if(mUpdateInfo!=null){
                    mUpdateInfo.NotifyDataRemoved(position);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        mFirebaseDatabase.getReference("units").child(mPath).child("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mDate = dataSnapshot.getValue(String.class);
                if(mUpdateInfo!=null) {
                    mUpdateInfo.UpdateInfo(mDate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addTimeSlot(TimeSlot c) {
        String key = mFirebaseDatabase.getReference("units").child(mPath).child("timeslots").push().getKey();
        c.setId(key);

        mFirebaseDatabase.getReference("units").child(mPath).child("timeslots").child(key).setValue(c);
    }

    public void removeAllTimeSlot(){
        mFirebaseDatabase.getReference("units").child(mPath).removeValue();
    }

    public void removeTimeSlot(int position){
        mFirebaseDatabase.getReference("units").child(mPath).child("timeslots").child(keyList.get(position)).removeValue();
    }

    public List<TimeSlot> getTimeSlots() {
        return mTimeSlots;
    }

    public void setDate(String date){
        mFirebaseDatabase.getReference("units").child(mPath).child("date").setValue(date);
    }
}

