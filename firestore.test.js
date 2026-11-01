const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

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

// 1. Unauthenticated checks
test("Unauthenticated: cannot access users or conversations", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").get());
  await assertFails(unauthDb.collection("conversations").get());
});

// 2. User profile tests
test("User: can create own profile and read profiles", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Mensah",
      handle: "@alice_m",
      createdAt: new Date(),
    })
  );

  // Bob can read Alice's public profile
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("users").doc(ALICE_UID).get());

  // Bob cannot modify Alice's profile
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hacked by Bob",
    })
  );
});

// 3. Conversation participation tests
test("Conversation: only participants can access", async () => {
  // Alice creates a 1:1 conversation with Bob
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const convRef = aliceDb.collection("conversations").doc("conv_alice_bob");

  await assertSucceeds(
    convRef.set({
      isGroup: false,
      name: "Alice & Bob",
      participants: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
    })
  );

  // Alice can get conversation
  await assertSucceeds(convRef.get());

  // Bob can get conversation
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("conversations").doc("conv_alice_bob").get());

  // Charlie (non-participant) cannot get conversation
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("conversations").doc("conv_alice_bob").get());
});

// 4. Message sending and subcollection authorization
test("Messages: only participants can send and read messages", async () => {
  // Seed conversation
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("conversations").doc("conv_test").set({
      isGroup: false,
      participants: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const msgRef = aliceDb
    .collection("conversations")
    .doc("conv_test")
    .collection("messages")
    .doc("msg_1");

  // Alice sends message
  await assertSucceeds(
    msgRef.set({
      conversationId: "conv_test",
      senderId: ALICE_UID,
      senderName: "Alice",
      text: "Akwaaba Bob! Ready for the Kente auction?",
      type: "TEXT",
      participants: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
    })
  );

  // Bob reads message
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb
      .collection("conversations")
      .doc("conv_test")
      .collection("messages")
      .doc("msg_1")
      .get()
  );

  // Bob updates reactions and readBy
  await assertSucceeds(
    bobDb
      .collection("conversations")
      .doc("conv_test")
      .collection("messages")
      .doc("msg_1")
      .update({
        readBy: [ALICE_UID, BOB_UID],
      })
  );

  // Charlie (non-participant) cannot send message
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(
    charlieDb
      .collection("conversations")
      .doc("conv_test")
      .collection("messages")
      .doc("msg_charlie")
      .set({
        conversationId: "conv_test",
        senderId: CHARLIE_UID,
        text: "I am trespassing!",
        type: "TEXT",
        createdAt: new Date(),
      })
  );
});
