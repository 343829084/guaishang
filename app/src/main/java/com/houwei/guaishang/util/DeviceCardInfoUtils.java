package com.houwei.guaishang.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.houwei.guaishang.bean.PhoneInfo;
import com.houwei.guaishang.bean.SMSInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ** on 2018/7/31.
 */

public class DeviceCardInfoUtils {

    public static List<PhoneInfo> getPhoneNumberFromMobile (Context context) {
        List<PhoneInfo> list = new ArrayList<PhoneInfo>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { "display_name", "sort_key", "contact_id",
                        "data1" }, null, null, null);
//        moveToNext方法返回的是一个boolean类型的数据
        while (cursor.moveToNext()) {
            //读取通讯录的姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //读取通讯录的号码
            String number = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int Id = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String Sortkey = getSortkey(cursor.getString(1));
            PhoneInfo phoneInfo = new PhoneInfo(name, number);
            list.add(phoneInfo);
        }
        cursor.close();
        return list;
    }

    private static Uri SMS_INBOX = Uri.parse("content://sms/");
    public static List<SMSInfo> getSmsFromPhone(Context context) {
        List<SMSInfo> list = new ArrayList<>();
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[] {"_id", "address", "person","body", "date", "type" };
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
        if (null == cur) {
            Log.i("ooc","************cur == null");
            return list;
        }
        while(cur.moveToNext()) {
            String number = cur.getString(cur.getColumnIndex("address"));//手机号
            String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
            String body = cur.getString(cur.getColumnIndex("body"));//短信内容
            //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("num",number);
            map.put("mess",body);
            list.add(new SMSInfo(name, number, body));
        }
        return list;
    }

    private static String getSortkey(String sortKeyString) {
        String key =sortKeyString.substring(0,1).toUpperCase();
        if (key.matches("[A-Z]")){
            return key;
        }else
            return "#";
    }
}
