/**
 * Defines the protocol which SomnoClient and SomnoServer share in order
 * to pass messages between them.
 * This interface contains string formatting, basic message types, et cetera.
 */

public interface SomnoProtocol {

    /** String headers */
    //logout message header
    String logoutHeader = "/logout";

    //password message header
    String pwdHeader = "/pwd";

    //user join message header
    String userJoinHeader = "/ujoin";

    //user leave message header
    String userLeaveHeader = "/uleave";

    //standard message header
    String stdMsgHeader = "/msg";

    /** String formats */
    //logout message format
    //logoutHeader + reason code
    String logoutMsg = logoutHeader + " %d";

    //password message format
    //passwordHeader + password
    String pwdMsg = pwdHeader + " %s";

    //really simple join, leave, and standard message stuff.
    String userJoin = userJoinHeader + " %s";

    String userLeave = userJoinHeader + " %s";

    String stdMsg = stdMsgHeader + " %s";

}
