curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiOWI0NTRlMzk3N2JlYjQ1ZTFhNzQ3ZTFkMjYwNWQ3YzEiLCJleHAiOjE3NDQzNDI2OTEyMTksInRpbWVzdGFtcCI6MTc0NDM0MDg5MTIyNX0.oL4klR_v-8EM-sTbaZiGs28kNtlWfp--q--RlbH-0Ac" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions