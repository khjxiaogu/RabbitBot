package com.khjxiaogu.RabbitBot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.OfflineGroupImage;

public class RabbitBot extends PluginBase {
	List<String> al;
	public Map<Long, Boolean> freezes = new HashMap<>();
	public Map<String,Boolean> rabbitmapping=new ConcurrentHashMap<>();
	Random rabbitrand = new Random();
	BufferedImage rabbithash;
	boolean cs;
	public void onEnable() {
		File cfgf=new File(this.getDataFolder(),"config.yml");
		if(!cfgf.exists()) {
			try {
				cfgf.createNewFile();
				this.getResources("config.yml").transferTo(new FileOutputStream(cfgf));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		al=this.loadConfig("config.yml").getStringList("rabbits");
		cs=this.loadConfig("config.yml").getBoolean("checkSimilarity");
		this.getEventListener().subscribeAlways(GroupMessageEvent.class, event -> {
			boolean curhasRabbit = false;
			boolean curfez=freezes.getOrDefault(event.getGroup().getId(), false);
			for (Message msg : event.getMessage()) {
				if (msg instanceof Image) {
					// getLogger().info(((Image) msg).getImageId());
					String iid=((Image) msg).getImageId();
					if (al.contains(iid)) {
						curhasRabbit = true;
						break;
					}
					Boolean israbit=rabbitmapping.get(iid);
					if(israbit!=null) {
						if(israbit) {
							curhasRabbit = true;
							break;
						}
						continue;
					}
					if(cs&&(!curfez)) {
						try {
							HttpURLConnection huc=(HttpURLConnection) new URL(event.getBot().queryImageUrl((Image) msg)).openConnection();
							huc.setRequestMethod("GET");
							huc.connect();
							BufferedImage normhash=PictureSimilarity.getThumbnil(huc.getInputStream());
							double simi=PictureSimilarity.compare(normhash,rabbithash);
							getLogger().info("similar"+simi);
							if(simi<7000) {
								curhasRabbit = true;
								rabbitmapping.put(iid,true);
								break;
							}
							rabbitmapping.put(iid,false);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			if (!curhasRabbit) {
				freezes.put(event.getGroup().getId(), false);
				return;
			}
			if (!curfez) {
				event.getGroup().sendMessage(new OfflineGroupImage(al.get(rabbitrand.nextInt(al.size()))));
				freezes.put(event.getGroup().getId(), true);
			}
		});
		try {
			rabbithash=PictureSimilarity.getThumbnil(this.getResources("rabbit.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getLogger().info("插件加载完毕!");
	}
}
