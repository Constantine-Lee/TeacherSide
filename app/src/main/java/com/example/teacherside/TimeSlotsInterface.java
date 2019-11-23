package com.example.teacherside;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotsInterface extends AppCompatActivity implements TimeSlotManager.UpdateInfo {

    private static final int START_NEW_SESSION_SCREEN = 0;
    private static final String UNIT_CODE = "UNIT_CODE";

    private RecyclerView mTimeSlotInterface_recyclerView;
    private static TimeSlotAdapter mTimeSlotInterface_recyclerView_Adapter;

    private TimeSlotManager mTimeSlotManager;
    private List<TimeSlot> mTimeSlots;
    private List<Unit> mUnitList = new ArrayList<>();

    private Button mButton_newSession;
    private TextView mTextView_date;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private String mUnitCode;
    private String userIdentityNo = firebaseUser.getEmail().substring(0, firebaseUser.getEmail().indexOf("@"));

    public static Intent newIntent(Context packageContext, String unitCode){
        Intent intent = new Intent (packageContext, TimeSlotsInterface.class);
        intent.putExtra(UNIT_CODE, unitCode);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUnitCode = getIntent().getStringExtra(UNIT_CODE);
        getSupportActionBar().setTitle(mUnitCode);

        mTimeSlotManager = new TimeSlotManager(this, mUnitCode);
        SetUpFirebaseListener();

        InitUI();
        SetUpRecyclerView();
        SetListener();
        updateUI();
    }

    private void SetUpFirebaseListener(){
        mFirebaseDatabase.getReference("professor").child(userIdentityNo).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mUnitList.add((Unit)dataSnapshot.getValue(Unit.class));
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void SetUpRecyclerView(){
        mTimeSlots = mTimeSlotManager.getTimeSlots();
        mTimeSlotInterface_recyclerView_Adapter = new TimeSlotAdapter(mTimeSlots);
        mTimeSlotInterface_recyclerView = (RecyclerView) findViewById(R.id.RECYCLERVIEW_TIMESLOTS);
        mTimeSlotInterface_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(mTimeSlotInterface_recyclerView_Adapter));
        itemTouchHelper.attachToRecyclerView(mTimeSlotInterface_recyclerView);
    }

    private void InitUI(){
        setContentView(R.layout.timeslots_interface);
        mTextView_date = (TextView) findViewById(R.id.TextView_timeSlotsInterface_date);
        mButton_newSession = (Button) findViewById(R.id.button_newSession);
    }

    private void SetListener(){
        mButton_newSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = NewSessionScreen.newIntent(TimeSlotsInterface.this, mUnitCode);
                startActivityForResult(intent, START_NEW_SESSION_SCREEN);
            }
        });
    }

    private void updateUI(){
        mTimeSlots = mTimeSlotManager.getTimeSlots();
        mTimeSlotInterface_recyclerView_Adapter = new TimeSlotAdapter(mTimeSlots);
        mTimeSlotInterface_recyclerView.setAdapter(mTimeSlotInterface_recyclerView_Adapter);
        mTimeSlotInterface_recyclerView_Adapter.notifyDataSetChanged();
    }

    public void UpdateInfo(String date) {
        mTextView_date.setText(date);
    }

    public void NotifyDataInserted(int position){
        mTimeSlotInterface_recyclerView_Adapter.notifyItemInserted(position);
    }

    public void NotifyDataRemoved(int position){
        mTimeSlotInterface_recyclerView_Adapter.notifyItemRemoved(position);
    }

    private class TimeSlotHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TimeSlot mTimeSlot;
        private TextView mTextView_time, mTextView_name;

        public TimeSlotHolder(View view){
            super(view);
            mTextView_time = (TextView) view.findViewById(R.id.RECYCLERVIEW_TEXTVIEW_TIME);
            mTextView_name = (TextView) view.findViewById(R.id.RECYCLERVIEW_TEXTVIEW_NAME);
            itemView.setOnClickListener(this);
        }

        private void bind(TimeSlot timeSlot){
            mTimeSlot = timeSlot;
            mTextView_time.setText(mTimeSlot.getTime());
            mTextView_name.setText(mTimeSlot.getName());
        }

        @Override
        public void onClick(View view) {
        }
    }
    public static void NotifyAdapterDataSetChanged(){
        mTimeSlotInterface_recyclerView_Adapter.notifyDataSetChanged();
    }

    public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotHolder>{
        public List<TimeSlot> mListOfTimeSlot;

        public TimeSlotAdapter(List<TimeSlot> ListOfTimeSlot){
            mListOfTimeSlot = ListOfTimeSlot;
        }

        @Override
        public TimeSlotHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.time_slot_item_material_design, parent, false);
            TimeSlotHolder viewHolder = new TimeSlotHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(TimeSlotHolder holder, int position) {
            holder.bind(mListOfTimeSlot.get(position));
        }

        public void setListOfTimeSlot(List<TimeSlot> listOfTimeSlot){
            mListOfTimeSlot = listOfTimeSlot;
        }

        @Override
        public int getItemCount() {
            return mListOfTimeSlot.size();
        }

        public void deleteItem(int position){
            mTimeSlotManager.removeTimeSlot(position);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }


    public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private TimeSlotAdapter mAdapter;
        private Drawable icon;
        private final ColorDrawable background;

        public SwipeToDeleteCallback(TimeSlotAdapter adapter){
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mAdapter = adapter;
            icon = ContextCompat.getDrawable(TimeSlotsInterface.this, android.R.drawable.ic_menu_close_clear_cancel);
            background = new ColorDrawable(Color.BLACK);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            mAdapter.deleteItem(position);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20; //so background is behind the rounded corners of itemView

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + icon.getIntrinsicWidth();
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft()+25, itemView.getTop()+12,
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset , itemView.getBottom()-12);
            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop()+12, itemView.getRight() -25, itemView.getBottom()-12);
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }

            background.draw(c);
            icon.draw(c);
        }
    }

}