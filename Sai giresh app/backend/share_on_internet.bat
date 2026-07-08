@echo off
title Share WealthWise AI on Public Internet
echo =======================================================================
echo              WealthWise AI Agent Public Sharing Tunnel
echo =======================================================================
echo.
echo Make sure your FastAPI server is running on localhost:8000 first!
echo (Run: uvicorn main:app --host 0.0.0.0 --port 8000 --reload)
echo.
echo Launching local tunnel using localhost.run (SSH)...
echo.
echo IMPORTANT: Copy the secure HTTPS link (e.g. https://xxxx.lhr.life)
echo shown in the output below. Open it on any phone or device worldwide!
echo.
echo Press Ctrl+C to close the tunnel when finished.
echo.
echo =======================================================================
ssh -R 80:localhost:8000 nokey@localhost.run
pause
