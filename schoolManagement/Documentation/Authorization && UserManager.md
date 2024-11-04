### LOGIN

There are three types of login operations available:

1. **Username & Password Login**
2. **OTP Login**
3. **Google Login**

---

![OTP Login Flow](https://res.cloudinary.com/drvhfm9in/image/upload/v1730704973/Screenshot_from_2024-11-04_12-46-37_mzh7op.png)

----

#### OTP Login (Login Type = `Fretron`)

The OTP login flow provides users with a one-time password sent to their registered mobile number for authentication.

1. **Initiate Login**:
   - The user enters their mobile number and submits it to the `POST /users/v1/user/login` API endpoint.
   - This endpoint verifies if there is a user in the database with the specified mobile number.
   - If a user is found, the system generates a random 6-digit OTP.

2. **OTP Generation**:
   - The OTP is generated using random numbers from `0` to `9`, producing a sequence of six digits.
   - The generated OTP is stored in the database, associated with the corresponding `userId`.

3. **Send OTP**:
   - The OTP is sent to the user via SMS using `CentralNotificationSvc.SendSMS`[notification manager].

4. **OTP Validation**:
   - The user enters the OTP they received and submits it by clicking the login button.
   - The `POST /users/v1/user/validate-otp` API endpoint is called, extracting the `mobileNumber` and `otp` from the request.
   - The system compares the entered OTP with the one stored in the database.
   - On successful validation, the API response includes:
     - `user.getUuid()`
     - `jwtToken`
     - `browserInfo`
     - `location`
     - `authService.parseTokenAndGetOrgId(jwtToken)`

5. **JWT Token Contents**:
   - The generated JWT token contains the following information:
     - `userId`
     - `email`
     - `mobileNumber`
     - `orgId`
     - `name`
     - `type`
     - `isGod`
     - `bpId`
     - `parentOrgId`
     - `portalType`
     - `childIds`

6. **Blocking Excessive Attempts**:
   - The `throwIfBlocked` function is used to manage excessive login attempts.
   - If a user exceeds the allowed number of login attempts within a specified period, a `NotAllowedException` is thrown.
   - This mechanism enforces a temporary block on further login attempts to prevent abuse.


#### UserName && Password Login (Login Type = `password`)


The Username & Password login flow allows users to authenticate using their mobile number and password.

1. **Initiate Login**:
   - The user submits their mobile number and password to the `/users/v1/user/login` endpoint.
   - The system checks if the user is blocked by calling `throwIfBlocked(mobileNumber)`.

2. **User and Password Validation**:
   - The system retrieves the user based on the `mobileNumber`.
   - If the user does not exist or the password is incorrect, an error is returned.
   - The entered password and hashed password in the database will be compared.

3. **Session Handling**:
   - If there’s no prior session, a new JWT token (`serverToken`) is generated.
   - If a previous session exists, it’s validated to confirm organization membership and permissions.

4. **Session Details**:
   - The login session is saved with `serverToken`, browser information, and location details.

5. **Response**:
   - The response includes:
     - `userType`: "existing"
     - `loginType`: "password"
     - `token`: Generated session token (`serverToken`)



#### Google Login (Login Type = `google`)

The Google login flow allows users to authenticate using their Google account.

1. **Token Extraction & Validation**:
   - The system retrieves the Google `authToken` from the request.
   - The `token` and optional `version` (defaulting to `v1`) are extracted.
   - Based on the version, the token is validated using `googleAuthTokenValidationService`.

2. **User Lookup**:
   - The user’s email is extracted from the token info and used to check if the user exists in the system.
   - If the user is new (not found), a response is prepared with:
     - `email`, `userType` set to "new", and the user’s `name`.
   - If the user exists, their account type is verified.

3. **Session Handling**:
   - If there’s no prior session, a new JWT token (`serverToken`) is generated.
   - If a previous session exists, it’s validated to confirm organization membership and permissions.

4. **Session Details**:
   - The login session is saved with the `serverToken`, browser info, and location.

5. **Response**:
   - The response includes:
     - `userType`: "new" or "existing"
     - `loginType`: "google"
     - `token`: Generated session token (`serverToken`) if the user is existing

