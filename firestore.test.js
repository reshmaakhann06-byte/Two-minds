const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ME_UID = "uid_me_887757";
const UBAID_UID = "uid_ubaid_887756";
const INTRUDER_UID = "uid_intruder_999";
const WORKSPACE_ID = "two_minds_main";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: denied all reads and writes", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ME_UID).get());
  await assertFails(unauthDb.collection("workspaces").doc(WORKSPACE_ID).get());
  await assertFails(
    unauthDb.collection("workspaces").doc(WORKSPACE_ID).set({
      workspaceName: "Two Minds",
      memberUids: [ME_UID],
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("User profile: owner can create & update; another user cannot read or write", async () => {
  const meDb = testEnv.authenticatedContext(ME_UID).firestore();
  const ubaidDb = testEnv.authenticatedContext(UBAID_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    meDb.collection("users").doc(ME_UID).set({
      userId: ME_UID,
      role: "me",
      displayName: "You (Partner)",
      themeMode: "dark",
      notificationsEnabled: true,
      disappearingMessagesSec: 0,
      blockedPartner: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    meDb.collection("users").doc(ME_UID).update({
      themeMode: "light",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cross-user isolation
  await assertFails(ubaidDb.collection("users").doc(ME_UID).get());
  await assertFails(
    ubaidDb.collection("users").doc(ME_UID).update({
      themeMode: "dark",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  // Shadow field injection must fail
  await assertFails(
    meDb.collection("users").doc(ME_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Workspace & MemberProfile: Me and Ubaid can read each other, but only owner can write their own section", async () => {
  const meDb = testEnv.authenticatedContext(ME_UID).firestore();
  const ubaidDb = testEnv.authenticatedContext(UBAID_UID).firestore();
  const intruderDb = testEnv.authenticatedContext(INTRUDER_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Me creates workspace
  await assertSucceeds(
    meDb.collection("workspaces").doc(WORKSPACE_ID).set({
      workspaceName: "Two Minds",
      memberUids: [ME_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Ubaid joins workspace (2nd member)
  await assertSucceeds(
    ubaidDb.collection("workspaces").doc(WORKSPACE_ID).update({
      memberUids: [ME_UID, UBAID_UID],
      updatedAt: new Date(Date.now() - 5000),
    })
  );

  // 3rd user cannot join
  await assertFails(
    intruderDb.collection("workspaces").doc(WORKSPACE_ID).update({
      memberUids: [ME_UID, UBAID_UID, INTRUDER_UID],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Me creates memberProfiles/me
  await assertSucceeds(
    meDb.collection("workspaces").doc(WORKSPACE_ID).collection("memberProfiles").doc("me").set({
      ownerUid: ME_UID,
      role: "me",
      displayName: "You (Partner)",
      online: true,
      typingInChat: false,
      overallPercent: 78,
      tasksDone: 12,
      tasksTotal: 15,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Ubaid creates memberProfiles/ubaid
  await assertSucceeds(
    ubaidDb.collection("workspaces").doc(WORKSPACE_ID).collection("memberProfiles").doc("ubaid").set({
      ownerUid: UBAID_UID,
      role: "ubaid",
      displayName: "Ubaid",
      online: true,
      typingInChat: false,
      overallPercent: 82,
      tasksDone: 13,
      tasksTotal: 15,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Me can READ Ubaid's profile, CANNOT UPDATE Ubaid's profile
  await assertSucceeds(
    meDb.collection("workspaces").doc(WORKSPACE_ID).collection("memberProfiles").doc("ubaid").get()
  );
  await assertFails(
    meDb.collection("workspaces").doc(WORKSPACE_ID).collection("memberProfiles").doc("ubaid").update({
      overallPercent: 10,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Intruder cannot read either profile
  await assertFails(
    intruderDb.collection("workspaces").doc(WORKSPACE_ID).collection("memberProfiles").doc("me").get()
  );
});

test("Encrypted Live Chat, Calls, Business Items & Book Notes: query alignment & strict permissions", async () => {
  const meDb = testEnv.authenticatedContext(ME_UID).firestore();
  const ubaidDb = testEnv.authenticatedContext(UBAID_UID).firestore();
  const intruderDb = testEnv.authenticatedContext(INTRUDER_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("workspaces").doc(WORKSPACE_ID).set({
      workspaceName: "Two Minds",
      memberUids: [ME_UID, UBAID_UID],
      createdAt: created,
      updatedAt: created,
    });
  });

  // Me sends E2E encrypted chat message
  await assertSucceeds(
    meDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages").doc("msg_1").set({
      senderUid: ME_UID,
      senderRole: "me",
      memberUids: [ME_UID, UBAID_UID],
      ciphertext: "dGVzdF9jaXBoZXJ0ZXh0",
      ivBase64: "MTIzNDU2Nzg5MDEy",
      messageType: "text",
      edited: false,
      deleted: false,
      pinned: false,
      readByUids: [ME_UID],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Ubaid queries messages with array-contains filter (succeeds); unfiltered fails
  await assertSucceeds(
    ubaidDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages")
      .where("memberUids", "array-contains", UBAID_UID).get()
  );
  await assertFails(
    ubaidDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages").get()
  );

  // Ubaid marks read receipt & reacts (succeeds), but cannot edit Me's ciphertext (fails)
  await assertSucceeds(
    ubaidDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages").doc("msg_1").update({
      readByUids: [ME_UID, UBAID_UID],
      reaction: "🔥",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    ubaidDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages").doc("msg_1").update({
      ciphertext: "hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Me edits own message (succeeds)
  await assertSucceeds(
    meDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages").doc("msg_1").update({
      ciphertext: "ZWRpdGVkX2NpcGhlcnRleHQ=",
      edited: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Intruder cannot read Me's messages or get doc directly or write
  await assertFails(
    intruderDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages")
      .where("memberUids", "array-contains", ME_UID).get()
  );
  await assertFails(
    intruderDb.collection("workspaces").doc(WORKSPACE_ID).collection("messages").doc("msg_1").get()
  );

  // Call signaling: Me starts call, Ubaid answers, then ends (terminal lock)
  await assertSucceeds(
    meDb.collection("workspaces").doc(WORKSPACE_ID).collection("calls").doc("call_1").set({
      callerUid: ME_UID,
      callerRole: "me",
      memberUids: [ME_UID, UBAID_UID],
      callType: "video",
      status: "ringing",
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(
    ubaidDb.collection("workspaces").doc(WORKSPACE_ID).collection("calls").doc("call_1").update({
      status: "ended",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  // Terminal state lock: cannot reopen ended call
  await assertFails(
    meDb.collection("workspaces").doc(WORKSPACE_ID).collection("calls").doc("call_1").update({
      status: "active",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("User-scoped Trading & CustomBooks: owner read/write succeeds; cross-user fails", async () => {
  const meDb = testEnv.authenticatedContext(ME_UID).firestore();
  const ubaidDb = testEnv.authenticatedContext(UBAID_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    meDb.collection("users").doc(ME_UID).collection("trades").doc("tr_1").set({
      userId: ME_UID,
      symbol: "BTCUSDT",
      side: "BUY",
      quantity: 0.25,
      priceUsd: 92000.0,
      totalUsd: 23000.0,
      createdAt: created,
      updatedAt: created,
    })
  );
  await assertSucceeds(meDb.collection("users").doc(ME_UID).collection("trades").get());
  await assertFails(ubaidDb.collection("users").doc(ME_UID).collection("trades").get());
});
