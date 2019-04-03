package io.github.takusan23.kaisendon;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lunger.draglistview.DragListAdapter;
import com.lunger.draglistview.DragListView;

import java.util.ArrayList;

public class TestDragListView extends AppCompatActivity {

    private DragListView dragListView;
    private ArrayList<String> mDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_drag_list_view);

        dragListView = findViewById(R.id.dragListView);
         mDataList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            mDataList.add("にゃーん : " + i);
        }
        dragListView.setDragListAdapter(new MyAdapter(this, mDataList));
        //设置点击item哪个部位可触发拖拽（可不设置，默认是item任意位置长按可拖拽）
        //dragListView.setDragger(R.id.iv_move);
        //设置item悬浮背景色
        //dragListView.setItemFloatColor(getString(R.string.float_color));
        //设置item悬浮透明度
        dragListView.setItemFloatAlpha(0.65f);
        //设置拖拽响应回调
        dragListView.setMyDragListener(new DragListView.MyDragListener() {
            @Override
            public void onDragFinish(int srcPositon, int finalPosition) {
                Toast.makeText(TestDragListView.this,
                        "beginPosition : " + srcPositon + "...endPosition : " + finalPosition,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    class MyAdapter extends DragListAdapter {

        public MyAdapter(Context context, ArrayList<String> arrayTitles) {
            super(context, arrayTitles);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            /***
             * 在这里尽可能每次都进行实例化新的，这样在拖拽ListView的时候不会出现错乱.
             * 具体原因不明，不过这样经过测试，目前没有发现错乱。虽说效率不高，但是做拖拽LisView足够了。
             */
            view = LayoutInflater.from(TestDragListView.this).inflate(
                    R.layout.drag_list_item, null);

            TextView textView = (TextView) view.findViewById(R.id.tv_name);
            textView.setText(mDataList.get(position));
            return view;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}
