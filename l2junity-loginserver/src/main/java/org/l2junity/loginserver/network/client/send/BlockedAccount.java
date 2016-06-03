/*
 * Copyright (C) 2004-2015 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2junity.loginserver.network.client.send;

import org.l2junity.network.IOutgoingPacket;
import org.l2junity.network.PacketWriter;

/**
 * @author NosBit
 */
public class BlockedAccount implements IOutgoingPacket
{
	/**
	 * Message: Your account has been restricted due to an account theft issue. If you have an email address registered to your account information, please check your inbox for an email message with details. If you have no direct connection to account theft, please visit the (font
	 * color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)) and file a petition. For more details, please visit (font color='#FFDF4C')1:1 Chat in Customer Service Center(/font).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_AN_ACCOUNT_THEFT_ISSUE_IF_YOU_HAVE_AN_EMAIL_ADDRESS_REGISTERED_TO_YOUR_ACCOUNT_INFORMATION_PLEASE_CHECK_YOUR_INBOX_FOR_AN_EMAIL_MESSAGE_WITH_DETAILS_IF_YOU_HAVE_NO_DIRECT_CONNECTION_TO_ACCOUNT_THEFT_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM_AND_FILE_A_PETITION_FOR_MORE_DETAILS_PLEASE_VISIT_1_1_CHAT_IN_CUSTOMER_SERVICE_CENTER = new BlockedAccount(0x01, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service due to your confirmed abuse of GM services or reporting services. For more details, please visit the (font color='#FFDF4C')website((/font)(font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)(font color='#FFDF4C')) 1:1 Customer Service Center(/font)(font color='#FFDF4C')) 1:1 Customer Service Center(/font).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_DUE_TO_YOUR_CONFIRMED_ABUSE_OF_GM_SERVICES_OR_REPORTING_SERVICES_FOR_MORE_DETAILS_PLEASE_VISIT_THE_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM_1_1_CUSTOMER_SERVICE_CENTER_1_1_CUSTOMER_SERVICE_CENTER = new BlockedAccount(0x02, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service as you failed to verify your identity within a given time after an account theft report. You may undo the restriction by visiting the (font color='#FFDF4C')Lineage II Support Website((/font)(font
	 * color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)(font color='#FFDF4C')) 1:1 Customer Service Center(font color='#FFDF4C')) Support Center(/font) and going through the identity verification process in the account theft report. For more details, please visit
	 * the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_AS_YOU_FAILED_TO_VERIFY_YOUR_IDENTITY_WITHIN_A_GIVEN_TIME_AFTER_AN_ACCOUNT_THEFT_REPORT_YOU_MAY_UNDO_THE_RESTRICTION_BY_VISITING_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x04, 0);
	
	/**
	 * Message: Your account has been restricted due to your abuse of game systems that resulted in damage to other players' gaming experience. For more details, please the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font))(font color='#FFDF4C'))1:1 Chat in Customer Service Center(/font).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_ABUSE_OF_GAME_SYSTEMS_THAT_RESULTED_IN_DAMAGE_TO_OTHER_PLAYERS_GAMING_EXPERIENCE_FOR_MORE_DETAILS_PLEASE_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM_1_1_CHAT_IN_CUSTOMER_SERVICE_CENTER = new BlockedAccount(0x08, 0);
	
	/**
	 * Message: Your account has been restricted due to your confirmed attempt at commercial advertising or trade involving cash or other games. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_CONFIRMED_ATTEMPT_AT_COMMERCIAL_ADVERTISING_OR_TRADE_INVOLVING_CASH_OR_OTHER_GAMES_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x10, 0);
	
	/**
	 * Message: Your account has been restricted due to your confirmed cash/account trade activities. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_CONFIRMED_CASH_ACCOUNT_TRADE_ACTIVITIES_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x20, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service due to misconduct or fraud. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_DUE_TO_MISCONDUCT_OR_FRAUD_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x40, 0);
	
	/**
	 * Message: Your account has been restricted due to the use of inappropriate language. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_THE_USE_OF_INAPPROPRIATE_LANGUAGE_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x80, 0);
	
	/**
	 * Message: Your account has been restricted due to your abuse of system weaknesses or bugs. Abusing bugs can cause serious system errors or destroy the game balance. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_ABUSE_OF_SYSTEM_WEAKNESSES_OR_BUGS_ABUSING_BUGS_CAN_CAUSE_SERIOUS_SYSTEM_ERRORS_OR_DESTROY_THE_GAME_BALANCE_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x100, 0);
	
	/**
	 * Message: Your account has been restricted due to development/distribution of an illegal program or modification of the server program. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_DEVELOPMENT_DISTRIBUTION_OF_AN_ILLEGAL_PROGRAM_OR_MODIFICATION_OF_THE_SERVER_PROGRAM_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM2 = new BlockedAccount(0x200, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service due to your confirmed abuse of in-game systems resulting in abnormal gameplay. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_DUE_TO_YOUR_CONFIRMED_ABUSE_OF_IN_GAME_SYSTEMS_RESULTING_IN_ABNORMAL_GAMEPLAY_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x400, 0);
	
	/**
	 * Message: Your account has been restricted at your request in accordance with our terms of service. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_AT_YOUR_REQUEST_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x800, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service due to your confirmed attempts at misconduct or fraud. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_DUE_TO_YOUR_CONFIRMED_ATTEMPTS_AT_MISCONDUCT_OR_FRAUD_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x1000, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service due to your fraudulent use of another person's identity. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_DUE_TO_YOUR_FRAUDULENT_USE_OF_ANOTHER_PERSON_S_IDENTITY_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x2000, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service due to your fraudulent transactions under another person's identity. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_DUE_TO_YOUR_FRAUDULENT_TRANSACTIONS_UNDER_ANOTHER_PERSON_S_IDENTITY_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x4000, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with our terms of service due to your confirmed in-game gambling activities. Please refer to the Support Center on the NCSOFT website (http://us.ncsoft.com/en/support) for more details.")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_OUR_TERMS_OF_SERVICE_DUE_TO_YOUR_CONFIRMED_IN_GAME_GAMBLING_ACTIVITIES_PLEASE_REFER_TO_THE_SUPPORT_CENTER_ON_THE_NCSOFT_WEBSITE_HTTP_US_NCSOFT_COM_EN_SUPPORT_FOR_MORE_DETAILS = new BlockedAccount(0x8000, 0);
	
	/**
	 * Message: Your account has been restricted due to your use of illegal programs. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_USE_OF_ILLEGAL_PROGRAMS_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x10000, 0);
	
	/**
	 * Message: Your account is temporarily restricted due to a complaint filed in the process of name changing. For more information, visit (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_IS_TEMPORARILY_RESTRICTED_DUE_TO_A_COMPLAINT_FILED_IN_THE_PROCESS_OF_NAME_CHANGING_FOR_MORE_INFORMATION_VISIT_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x20000, 0);
	
	/**
	 * Message: Please verify your identity to confirm your ownership of your account at the NCsoft website. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount PLEASE_VERIFY_YOUR_IDENTITY_TO_CONFIRM_YOUR_OWNERSHIP_OF_YOUR_ACCOUNT_AT_THE_NCSOFT_WEBSITE_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x40000, 0);
	
	/**
	 * Message: Your account has been restricted in accordance with an official request from an investigative agency (private law). This action was taken because the official request from the investigative agency has legal force. For more details, please visit the (font color='#FFDF4C')Lineage II
	 * Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_IN_ACCORDANCE_WITH_AN_OFFICIAL_REQUEST_FROM_AN_INVESTIGATIVE_AGENCY_PRIVATE_LAW_THIS_ACTION_WAS_TAKEN_BECAUSE_THE_OFFICIAL_REQUEST_FROM_THE_INVESTIGATIVE_AGENCY_HAS_LEGAL_FORCE_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x80000, 0);
	
	/**
	 * Message: Your account has been temporarily restricted due to acquisition of an item connected to account theft. Please visit the homepage and go through the personal verification process to lift the restriction. For more details, please visit the (font color='#FFDF4C')Lineage II Support
	 * Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_TEMPORARILY_RESTRICTED_DUE_TO_ACQUISITION_OF_AN_ITEM_CONNECTED_TO_ACCOUNT_THEFT_PLEASE_VISIT_THE_HOMEPAGE_AND_GO_THROUGH_THE_PERSONAL_VERIFICATION_PROCESS_TO_LIFT_THE_RESTRICTION_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x100000, 0);
	
	/**
	 * Message: Your account has been restricted due to your confirmed trade involving cash or other games. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_CONFIRMED_TRADE_INVOLVING_CASH_OR_OTHER_GAMES_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x200000, 0);
	
	/**
	 * Message: You cannot use the game services as your identity has not been verified. Please the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)) and go to (font color='#FFDF4C'))(/font) to verify
	 * your identity. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOU_CANNOT_USE_THE_GAME_SERVICES_AS_YOUR_IDENTITY_HAS_NOT_BEEN_VERIFIED_PLEASE_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x400000, 0);
	
	/**
	 * Message: Your account has been restricted due to your use of illegal programs. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_USE_OF_ILLEGAL_PROGRAMS_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM2 = new BlockedAccount(0x800000, 0);
	
	/**
	 * Message: Your account has been restricted due to your unfair acquisition of items and disregard for item distribution rules agreed upon by members of your party. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_UNFAIR_ACQUISITION_OF_ITEMS_AND_DISREGARD_FOR_ITEM_DISTRIBUTION_RULES_AGREED_UPON_BY_MEMBERS_OF_YOUR_PARTY_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x1000000, 0);
	
	/**
	 * Message: Your account has been denied all game services due to your confirmed use of the game for commercial purposes. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICES_DUE_TO_YOUR_CONFIRMED_USE_OF_THE_GAME_FOR_COMMERCIAL_PURPOSES_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x2000000, 0);
	
	/**
	 * Message: The account has been temporarily restricted due to an incomplete cell phone (ARS) transaction. For more information, please visit http://us.ncsoft.com/en/.")
	 */
	public static final BlockedAccount THE_ACCOUNT_HAS_BEEN_TEMPORARILY_RESTRICTED_DUE_TO_AN_INCOMPLETE_CELL_PHONE_ARS_TRANSACTION_FOR_MORE_INFORMATION_PLEASE_VISIT_HTTP_US_NCSOFT_COM_EN = new BlockedAccount(0x4000000, 0);
	
	/**
	 * Message: Your account has been restricted due to your confirmed attempt at commercial advertising. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_CONFIRMED_ATTEMPT_AT_COMMERCIAL_ADVERTISING_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x8000000, 0);
	
	/**
	 * Message: Your identity verification has been temporarily suspended due to suspected account theft. If you are not involved in account theft, please verify your identity by clicking the 'Release' button in the login page. For more details, please visit the (font color='#FFDF4C')Lineage II
	 * Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_IDENTITY_VERIFICATION_HAS_BEEN_TEMPORARILY_SUSPENDED_DUE_TO_SUSPECTED_ACCOUNT_THEFT_IF_YOU_ARE_NOT_INVOLVED_IN_ACCOUNT_THEFT_PLEASE_VERIFY_YOUR_IDENTITY_BY_CLICKING_THE_RELEASE_BUTTON_IN_THE_LOGIN_PAGE_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x10000000, 0);
	
	/**
	 * Message: Your account has been temporarily restricted due to your speculated abnormal methods of gameplay. If you did not employ abnormal means to play the game, please visit the website and go through the personal verification process to lift the restriction. For more details, please visit
	 * the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_TEMPORARILY_RESTRICTED_DUE_TO_YOUR_SPECULATED_ABNORMAL_METHODS_OF_GAMEPLAY_IF_YOU_DID_NOT_EMPLOY_ABNORMAL_MEANS_TO_PLAY_THE_GAME_PLEASE_VISIT_THE_WEBSITE_AND_GO_THROUGH_THE_PERSONAL_VERIFICATION_PROCESS_TO_LIFT_THE_RESTRICTION_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x20000000, 0);
	
	/**
	 * Message: Your account has been restricted due to your abuse of system weaknesses or bugs. Abusing bugs can cause grievous system errors or destroy the game balance. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_ABUSE_OF_SYSTEM_WEAKNESSES_OR_BUGS_ABUSING_BUGS_CAN_CAUSE_GRIEVOUS_SYSTEM_ERRORS_OR_DESTROY_THE_GAME_BALANCE_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x40000000, 0);
	
	/**
	 * Message: Your account is temporarily restricted due to a complaint filed in the process of name changing. For more information,visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_IS_TEMPORARILY_RESTRICTED_DUE_TO_A_COMPLAINT_FILED_IN_THE_PROCESS_OF_NAME_CHANGING_FOR_MORE_INFORMATION_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0x80000000, 0);
	
	/**
	 * Message: Your account has been denied all game service at your request. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICE_AT_YOUR_REQUEST_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x01);
	
	/**
	 * Message: Your account has been denied all game services due to your confirmed use of illegal programs. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICES_DUE_TO_YOUR_CONFIRMED_USE_OF_ILLEGAL_PROGRAMS_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x02);
	
	/**
	 * Message: Your account has been denied all game services due to your confirmed use of illegal programs. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICES_DUE_TO_YOUR_CONFIRMED_USE_OF_ILLEGAL_PROGRAMS_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM2 = new BlockedAccount(0, 0x04);
	
	/**
	 * Message: Your account has been restricted for a duration of 10 days due to your use of illegal programs. All game services are denied for the aforementioned period, and a repeated offense will result in a permanent ban. For more details, please visit the (font color='#FFDF4C')Lineage II
	 * Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_FOR_A_DURATION_OF_10_DAYS_DUE_TO_YOUR_USE_OF_ILLEGAL_PROGRAMS_ALL_GAME_SERVICES_ARE_DENIED_FOR_THE_AFOREMENTIONED_PERIOD_AND_A_REPEATED_OFFENSE_WILL_RESULT_IN_A_PERMANENT_BAN_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x08);
	
	/**
	 * Message: your account has been denied all game services due to your confirmed account trade history. (br)For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICES_DUE_TO_YOUR_CONFIRMED_ACCOUNT_TRADE_HISTORY_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x10);
	
	/**
	 * Message: Your account has been denied all game services due to transaction fraud. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICES_DUE_TO_TRANSACTION_FRAUD_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x20);
	
	/**
	 * Message: Your account has been temporarily denied all game services due to connections with account registration done by means of identity theft. If you have no connection to the issue, please go through the personal verification process. For more details, please visit the (font
	 * color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_TEMPORARILY_DENIED_ALL_GAME_SERVICES_DUE_TO_CONNECTIONS_WITH_ACCOUNT_REGISTRATION_DONE_BY_MEANS_OF_IDENTITY_THEFT_IF_YOU_HAVE_NO_CONNECTION_TO_THE_ISSUE_PLEASE_GO_THROUGH_THE_PERSONAL_VERIFICATION_PROCESS__FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x40);
	
	/**
	 * Message: Your account has been denied all game services due to your confirmed use of the game for commercial purposes. For more details, please visit (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICES_DUE_TO_YOUR_CONFIRMED_USE_OF_THE_GAME_FOR_COMMERCIAL_PURPOSES_FOR_MORE_DETAILS_PLEASE_VISIT_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x80);
	
	/**
	 * Message: Your account has been restricted due to frequent posting of inappropriate content. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_FREQUENT_POSTING_OF_INAPPROPRIATE_CONTENT_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x100);
	
	/**
	 * Message: Your account has been restricted due to a confirmed post in violation of the law. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_A_CONFIRMED_POST_IN_VIOLATION_OF_THE_LAW_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x200);
	
	/**
	 * Message: Your account has been restricted due to your confirmed abuse of free NCoin. For more information, please visit http://us.ncsoft.com/en/.")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_CONFIRMED_ABUSE_OF_FREE_NCOIN_FOR_MORE_INFORMATION_PLEASE_VISIT_HTTP_US_NCSOFT_COM_EN = new BlockedAccount(0, 0x400);
	
	/**
	 * Message: Your account has been restricted due to your confirmed abuse of a bug pertaining to the NCoin. For more information, please visit http://us.ncsoft.com/en/.")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_YOUR_CONFIRMED_ABUSE_OF_A_BUG_PERTAINING_TO_THE_NCOIN_FOR_MORE_INFORMATION_PLEASE_VISIT_HTTP_US_NCSOFT_COM_EN = new BlockedAccount(0, 0x800);
	
	/**
	 * Message: All permissions on your account are restricted. (br)Please go to http://us.ncsoft.com/en/ for details.")
	 */
	public static final BlockedAccount ALL_PERMISSIONS_ON_YOUR_ACCOUNT_ARE_RESTRICTED_BR_PLEASE_GO_TO_HTTP_US_NCSOFT_COM_EN_FOR_DETAILS = new BlockedAccount(0, 0x1000);
	
	/**
	 * Message: All permissions on your account are restricted. (br)Please go to http://us.ncsoft.com/en/ for details.")
	 */
	public static final BlockedAccount ALL_PERMISSIONS_ON_YOUR_ACCOUNT_ARE_RESTRICTED_BR_PLEASE_GO_TO_HTTP_US_NCSOFT_COM_EN_FOR_DETAILS2 = new BlockedAccount(0, 0x2000);
	
	/**
	 * Message: Your account has been denied all game services due to its confirmed registration under someone else's identity. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_DENIED_ALL_GAME_SERVICES_DUE_TO_ITS_CONFIRMED_REGISTRATION_UNDER_SOMEONE_ELSE_S_IDENTITY_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x4000);
	
	/**
	 * Message: All permissions on your account are restricted. (br)Please go to http://us.ncsoft.com/en/ for details.")
	 */
	public static final BlockedAccount ALL_PERMISSIONS_ON_YOUR_ACCOUNT_ARE_RESTRICTED_BR_PLEASE_GO_TO_HTTP_US_NCSOFT_COM_EN_FOR_DETAILS3 = new BlockedAccount(0, 0x8000);
	
	/**
	 * Message: Your account has been restricted due to development/distribution of an illegal program or modification of the server program. For more details, please visit the (font color='#FFDF4C')Lineage II Support Website(/font)((font color='#6699FF')(a
	 * href='asfunction:homePage')https://support.lineage2.com(/a)(/font)).")
	 */
	public static final BlockedAccount YOUR_ACCOUNT_HAS_BEEN_RESTRICTED_DUE_TO_DEVELOPMENT_DISTRIBUTION_OF_AN_ILLEGAL_PROGRAM_OR_MODIFICATION_OF_THE_SERVER_PROGRAM_FOR_MORE_DETAILS_PLEASE_VISIT_THE_LINEAGE_II_SUPPORT_WEBSITE_HTTPS_SUPPORT_LINEAGE2_COM = new BlockedAccount(0, 0x10000);
	
	private final int _mask1;
	private final int _mask2;
	
	private BlockedAccount(int mask1, int mask2)
	{
		_mask1 = mask1;
		_mask2 = mask2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2junity.network.IOutgoingPacket#write(org.l2junity.network.PacketWriter)
	 */
	@Override
	public boolean write(PacketWriter packet)
	{
		packet.writeC(0x02);
		packet.writeD(_mask1);
		packet.writeD(_mask2);
		return true;
	}
}