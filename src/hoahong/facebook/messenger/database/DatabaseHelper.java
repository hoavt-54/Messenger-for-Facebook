/*
 * Copyright (c) 2014 Pendulab Inc.
 * 111 N Chestnut St, Suite 200, Winston Salem, NC, 27101, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Pendulab Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Pendulab.
 */

package hoahong.facebook.messenger.database;

import hoahong.facebook.messenger.data.ChatMessage;
import hoahong.facebook.messenger.data.ChatSession;
import hoahong.facebook.messenger.data.User;
import hoahong.facebook.messenger.data.UserGroup;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper{
	// name of the database file for Moomee application
	private static final String DATABASE_NAME = "hoahong.facebook.messenger.db";
	//database version
	private static final int DATABASE_VERSION = 3;
	
	private Dao<User, String> userDao = null;
	private Dao<UserGroup, String> userGroupDao = null;
	private Dao<ChatMessage, Integer> chatMessageDao = null;
	private Dao<ChatSession, Integer> chatSessionDao;

	// get DAO object for chat_sessions table
	public Dao<User, String> getUserDao() throws SQLException{
		if (userDao == null){
			userDao = getDao(User.class);
		}
		return userDao;
	}
	
	public Dao<UserGroup, String> getUserGroupDao () throws SQLException{
		if (userGroupDao == null)
			userGroupDao = getDao(UserGroup.class);
		return userGroupDao;
	}
	
	public Dao<ChatMessage, Integer> getMessageDao () throws SQLException{
		if (chatMessageDao == null){
			chatMessageDao = getDao(ChatMessage.class);
		}
		return chatMessageDao;
	}
	
	public Dao<ChatSession, Integer> getChatSessionDao() throws SQLException{
		if (chatSessionDao == null)
			chatSessionDao = getDao(ChatSession.class);
		return chatSessionDao;
	}
	
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}
	
	public void delelteOldMessage () throws SQLException{
		TableUtils.dropTable(connectionSource, ChatMessage.class, true);
		TableUtils.createTableIfNotExists(connectionSource, ChatMessage.class);
		TableUtils.dropTable(connectionSource, ChatSession.class, true);
		TableUtils.createTableIfNotExists(connectionSource, ChatSession.class);
	}
	
	
	public void cleanUsers() throws SQLException{
		TableUtils.dropTable(connectionSource, User.class, true);
		TableUtils.createTableIfNotExists(connectionSource, User.class);
	}
	
	
	/**
	 * This is called when the database is first created. In this method, all the table will be created.
	 */
	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		try {
			// here we create tables
			TableUtils.createTableIfNotExists(connectionSource, User.class);
			TableUtils.createTableIfNotExists(connectionSource, UserGroup.class);
			TableUtils.createTableIfNotExists(connectionSource, ChatMessage.class);
			TableUtils.createTableIfNotExists(connectionSource, ChatSession.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connection, int arg2,
			int arg3) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, ChatMessage.class, true);
			TableUtils.dropTable(connectionSource, ChatSession.class, true);
			TableUtils.dropTable(connectionSource, UserGroup.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		userDao = null;
		userGroupDao=null;
	}

}
