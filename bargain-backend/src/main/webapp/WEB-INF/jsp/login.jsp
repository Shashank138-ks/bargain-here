<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Bargain Here — Login</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link href="https://fonts.googleapis.com/css2?family=Syne:wght@400;600;700;800&family=DM+Sans:wght@300;400;500&display=swap" rel="stylesheet"/>
    <style>
        :root {
            --bg:        #060912;
            --surface:   #0d1321;
            --surface2:  #141b2d;
            --border:    rgba(255,255,255,0.07);
            --accent:    #00e5a0;
            --accent2:   #00b5ff;
            --text:      #e8edf5;
            --muted:     #6b7a99;
            --radius:    14px;
            --glow:      0 0 40px rgba(0,229,160,0.15);
        }

        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            background: var(--bg);
            color: var(--text);
            font-family: 'DM Sans', sans-serif;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            position: relative;
        }

        /* ── PREMIUM CITYSCAPE BACKGROUND (Matches Dashboard perfectly) ── */
        body::before {
            content: '';
            position: fixed; inset: 0;
            background:
                linear-gradient(rgba(6, 9, 18, 0.88), rgba(6, 9, 18, 0.94)),
                url('/bargain_bg.jpg') no-repeat center center / cover;
            pointer-events: none; z-index: 0;
        }

        .login-container {
            position: relative;
            z-index: 5;
            width: 100%;
            max-width: 420px;
            padding: 24px;
        }

        /* ── GLASSMORPHIC CARD ── */
        .login-card {
            background: rgba(13, 19, 33, 0.7);
            border: 1.5px solid var(--border);
            border-radius: var(--radius);
            padding: 40px 32px;
            backdrop-filter: blur(16px);
            box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
            text-align: center;
            animation: fadeUp 0.5s ease;
        }

        .logo {
            font-family: 'Syne', sans-serif;
            font-size: 2rem; font-weight: 800;
            letter-spacing: -0.03em;
            background: linear-gradient(135deg, var(--accent), var(--accent2));
            -webkit-background-clip: text; -webkit-text-fill-color: transparent;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
        }
        
        .logo-subtitle {
            color: var(--muted);
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            margin-bottom: 32px;
        }

        .form-group {
            text-align: left;
            margin-bottom: 20px;
        }

        .form-label {
            font-size: 0.75rem;
            font-weight: 500;
            color: var(--muted);
            letter-spacing: 0.08em;
            text-transform: uppercase;
            display: block;
            margin-bottom: 8px;
        }

        .form-input {
            width: 100%;
            background: var(--surface2);
            border: 1.5px solid var(--border);
            border-radius: 8px;
            padding: 14px 16px;
            font-family: 'DM Sans', sans-serif;
            font-size: 0.95rem;
            color: var(--text);
            outline: none;
            transition: border-color 0.3s, box-shadow 0.3s;
        }

        .form-input::placeholder {
            color: var(--muted);
            opacity: 0.7;
        }

        .form-input:focus {
            border-color: var(--accent);
            box-shadow: 0 0 15px rgba(0, 229, 160, 0.2);
        }

        .login-btn {
            width: 100%;
            background: linear-gradient(135deg, var(--accent), #00c87a);
            border: none; cursor: pointer;
            padding: 14px;
            border-radius: 999px;
            font-family: 'Syne', sans-serif;
            font-weight: 700; font-size: 0.95rem;
            color: #060912;
            letter-spacing: 0.03em;
            transition: transform 0.2s, opacity 0.2s;
            margin-top: 10px;
        }

        .login-btn:hover {
            transform: scale(1.02);
        }

        .login-btn:active {
            transform: scale(0.98);
        }

        @keyframes fadeUp {
            from { opacity: 0; transform: translateY(20px); }
            to   { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>

<div class="login-container">
    <div class="login-card">
        <div class="logo">
            <svg class="logo-icon" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="url(#logo-grad)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="filter: drop-shadow(0 0 10px rgba(0, 229, 160, 0.45)); flex-shrink: 0;">
                <defs>
                    <linearGradient id="logo-grad" x1="0%" y1="0%" x2="100%" y2="100%">
                        <stop offset="0%" stop-color="var(--accent)"></stop>
                        <stop offset="100%" stop-color="var(--accent2)"></stop>
                    </linearGradient>
                </defs>
                <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"></path>
                <circle cx="7" cy="7" r="1" fill="url(#logo-grad)" stroke="none"></circle>
            </svg>
            Bargain Here
        </div>
        <div class="logo-subtitle">Access Lowest Price Comparisons</div>

        <form action="/login" method="POST">
            <div class="form-group">
                <label class="form-label" for="username">User Name</label>
                <input class="form-input" type="text" id="username" name="username" placeholder="Enter your username" required autocomplete="off"/>
            </div>

            <div class="form-group">
                <label class="form-label" for="phoneNumber">Phone Number</label>
                <input class="form-input" type="tel" id="phoneNumber" name="phoneNumber" placeholder="Enter your phone number" required pattern="[0-9]{10,15}" title="Please enter a valid phone number (10 to 15 digits)" autocomplete="off"/>
            </div>

            <button type="submit" class="login-btn">Login ↗</button>
        </form>
    </div>
</div>

</body>
</html>
