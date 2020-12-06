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

    /** String formats */
    //logout message format
    //logoutHeader + reason
    String logoutMsg = logoutHeader + " %d";

    //password message format
    //passwordHeader + password
    String pwdMsg = pwdHeader + " %d";

}
