/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.houwei.guaishang.easemob;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Direct;
import com.easemob.chat.VoiceMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.houwei.guaishang.R;
import com.easemob.util.EMLog;

/**
 * 语音row播放点击事件监听
 *
 */
public class EaseChatRowVoicePlayClickListener implements View.OnClickListener {
	private static final String TAG = "VoicePlayClickListener";
	EMMessage message;
	VoiceMessageBody voiceBody;
	ImageView voiceIconView;

	private AnimationDrawable voiceAnimation = null;
	MediaPlayer mediaPlayer = null;
	ImageView iv_read_status;
	Activity activity;
	private ChatType chatType;
	private BaseAdapter adapter;

	public static boolean isPlaying = false;
	public static EaseChatRowVoicePlayClickListener currentPlayListener = null;
	public static String playMsgId;

	public EaseChatRowVoicePlayClickListener(EMMessage message, ImageView v, ImageView iv_read_status, BaseAdapter adapter, Activity context) {
		this.message = message;
		voiceBody = (VoiceMessageBody) message.getBody();
		this.iv_read_status = iv_read_status;
		this.adapter = adapter;
		voiceIconView = v;
		this.activity = context;
		this.chatType = message.getChatType();
	}

	public void stopPlayVoice() {
		voiceAnimation.stop();
		if (message.direct == EMMessage.Direct.RECEIVE) {
			voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
		} else {
			voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
		}
		// stop play voice
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		// 判断的当前播放的这条语音是否是阅后即焚，并且是接收方的消息，如果是 停止播放后删除这条消息
		if(message.getBooleanAttribute(EaseConstant.EASE_ATTR_READFIRE, false)
				&& message.direct == Direct.RECEIVE){
	        // 听完之后，发送ACK并删除消息
	        sendACKMessage();
		}
		isPlaying = false;
		playMsgId = null;
		
	}
	
	/**
     * ACK 消息的发送，根据是否发送成功做些相应的操作，这里是把发送失败的消息id和username保存在序列化类中
     */
    private void sendACKMessage() {
        try {
            EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
            message.isAcked = true;
        } catch (EaseMobException e) {
            e.printStackTrace();
            // 发送ACK 失败，将ack信息保存在序列化的类中
            EaseACKUtil.getInstance(activity).saveACKDataId(message.getMsgId(), message.getFrom());
        } finally {
        	VoiceMessageBody body = (VoiceMessageBody) message.getBody();
			File file = new File(body.getLocalUrl());
	        if (file.exists() && file.isFile()) {
	            file.delete();
	        };
            EMChatManager.getInstance().getConversation(message.getFrom()).removeMessage(message.getMsgId());
			if(adapter instanceof EaseMessageAdapter){
	            ((EaseMessageAdapter) adapter).refresh();
	        }else{
	            adapter.notifyDataSetChanged();
	        }
        }
    }

	public void playVoice(String filePath) {
		if (!(new File(filePath).exists())) {
			return;
		}
		playMsgId = message.getMsgId();
		AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

		mediaPlayer = new MediaPlayer();
		if (EaseUI.getInstance().getSettingsProvider().isSpeakerOpened()) {
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		} else {
			audioManager.setSpeakerphoneOn(false);// 关闭扬声器
			// 把声音设定成Earpiece（听筒）出来，设定为正在通话中
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}
		try {
			mediaPlayer.setDataSource(filePath);
			mediaPlayer.prepare();
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					mediaPlayer.release();
					mediaPlayer = null;
					stopPlayVoice(); // stop animation
				} 

			});
			isPlaying = true;
			currentPlayListener = this;
			mediaPlayer.start();
			showAnimation();

			// 如果是接收的消息
			if (message.direct == EMMessage.Direct.RECEIVE) {
				try {
					if (!message.isAcked) {
						message.isAcked = true;
						// 告知对方已读这条消息
						if (chatType != ChatType.GroupChat && chatType != ChatType.ChatRoom)
							EMChatManager.getInstance().ackMessageRead(message.getFrom(), message.getMsgId());
					}
				} catch (Exception e) {
					message.isAcked = false;
				}
				if (!message.isListened() && iv_read_status != null && iv_read_status.getVisibility() == View.VISIBLE) {
					// 隐藏自己未播放这条语音消息的标志
					iv_read_status.setVisibility(View.INVISIBLE);
					EMChatManager.getInstance().setMessageListened(message);
				}

			}

		} catch (Exception e) {
		    System.out.println();
		}
	}

	// show the voice playing animation
	private void showAnimation() {
		// play voice, and start animation
		if (message.direct == EMMessage.Direct.RECEIVE) {
			voiceIconView.setImageResource(R.drawable.voice_from_icon);
		} else {
			voiceIconView.setImageResource(R.drawable.voice_to_icon);
		}
		voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
		voiceAnimation.start();
	}

	@Override
	public void onClick(View v) {
		String st = activity.getResources().getString(R.string.Is_download_voice_click_later);
		if (isPlaying) {
			if (playMsgId != null && playMsgId.equals(message.getMsgId())) {
				currentPlayListener.stopPlayVoice();
				return;
			}
			currentPlayListener.stopPlayVoice();
		}

		if (message.direct == EMMessage.Direct.SEND) {
			// for sent msg, we will try to play the voice file directly
			playVoice(voiceBody.getLocalUrl());
		} else {
			if (message.status == EMMessage.Status.SUCCESS) {
				File file = new File(voiceBody.getLocalUrl());
				if (file.exists() && file.isFile())
					playVoice(voiceBody.getLocalUrl());
				else
					EMLog.e(TAG, "file not exist");

			} else if (message.status == EMMessage.Status.INPROGRESS) {
				Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
			} else if (message.status == EMMessage.Status.FAIL) {
				Toast.makeText(activity, st, Toast.LENGTH_SHORT).show();
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						EMChatManager.getInstance().asyncFetchMessage(message);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						super.onPostExecute(result);
						adapter.notifyDataSetChanged();
					}

				}.execute();

			}

		}
	}
}