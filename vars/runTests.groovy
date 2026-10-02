def call() {
    sh 'pip3 install -r requirements.txt --break-system-packages || true'
    echo 'No unit tests yet - placeholder stage'
}