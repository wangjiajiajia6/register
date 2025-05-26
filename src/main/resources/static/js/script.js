function usernameCheck(){
    const usernameInput=document.getElementById('username').value;
    const usernameOutput=document.getElementById('usernameOutput');
    if(usernameInput !== ""){
        if(usernameInput.length === 6){
            usernameOutput.innerHTML="OK!";
            return true;
        }
        else{
            usernameOutput.innerHTML="Your username must be six digit!";
            return false;
        }
    }
    else{
        usernameOutput.innerHTML="Your username can not be empty!"
        return false;
    }
}
function passwordCheck(){
    const passwordInput=document.getElementById('password').value;
    const passwordOutput=document.getElementById('passwordOutput');
    if(passwordInput !== ""){
        if(passwordInput.length === 8){
            passwordOutput.innerHTML="OK!";
            return true;
        }
        else{
            passwordOutput.innerHTML="Your password must be eight digit!";
            return false;
        }
    }
    else{
        passwordOutput.innerHTML="Your password can not be empty!"
        return false;
    }
}

function passwordRepeatCheck(){
    const passwordInputRepeat=document.getElementById('passwordRepeat').value;
    const passwordOutputRepeat=document.getElementById('passwordOutputRepeat');
    const passwordInput=document.getElementById('password').value;
    if(passwordInputRepeat !== ""){
        if(passwordInputRepeat === passwordInput){
            passwordOutputRepeat.innerHTML="OK!"
            return true;
        }
        else{
            passwordOutputRepeat.innerHTML="The password must be the same twice!";
            return false;
        }
    }
    else{
        passwordOutputRepeat.innerHTML="Your password cannot be empty!";
        return false;
    }
}

async function submit() {
    const usernameCheckResult = usernameCheck();
    const passwordCheckResult = passwordCheck();
    const passwordCheckRepeatResult = passwordRepeatCheck();
    const result = document.getElementById('result');

    if (!usernameCheckResult || !passwordCheckResult || !passwordCheckRepeatResult) {
        result.innerHTML = "The form did not pass validation, please check and enter again";
        result.style.color = "darkred";
        return false;
    }

    const formData = {
        username: document.getElementById('username').value,
        password: document.getElementById('password').value,
    };

    try {
        const response = await fetch('http://localhost:8080/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: document.getElementById('username').value,
                password: document.getElementById('password').value
            }),
            // 重要：添加这些选项
            mode: 'cors', // 明确使用CORS模式
            cache: 'no-cache',
            credentials: 'same-origin' // 根据后端需求调整
        });

        // 关键修改：先检查响应状态
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log("完整响应:", data);

        // 显示结果
        result.innerHTML = data.message;
        result.style.color = data.success ? "green" : "darkred";

    } catch (error) {
        console.error("完整错误:", error);
        result.innerHTML = error.message || "Registration failed";
        result.style.color = "darkred";
    }
}